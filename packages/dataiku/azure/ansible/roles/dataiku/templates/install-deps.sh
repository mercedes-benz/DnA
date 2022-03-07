#!/bin/bash -e

# Installs or checks the prerequisite packages for Dataiku Innovation Studio
# Supports the following Linux distributions:
# - Debian 9, 10
# - Ubuntu 16.04, 18.04, 20.04
# - RedHat/CentOS 6.8+, 7.0+
# - RedHat 8.0+
# - Amazon Linux 2017.xx, 2018.03
# - Amazon Linux 2
# - Oracle Linux 6.8+, 7.1+
# - SLES 12.2 to 12.5, 15, 15.1
# Supports the following non-Linux distributions:
# - OS X 10.12 (Mavericks) to 10.15 (Catalina)
# To be run as root for install, as any user for check

#
# Parse arguments
#
Usage() {
	echo "
Usage: $0 [OPTIONS]
Available options:
	-check : check dependencies only, do not install
	-yes : do not prompt before package installation, assume yes

	-without-java : do not check nor install for Java environment

	-without-python : do not check nor install for Python and Python packages dependencies
	-with-conda : include dependencies for Conda packages - implies -without-python

	-with-r : include dependencies for R integration

	-with-chrome : include dependencies for dashboard export

	-os DISTRIB VERSION : force OS detection result
" >&2
	exit 1
}

check_only=
yes=
without_java=
without_python=
with_r=
with_chrome=
distrib=
distribVersion=
while [ $# -gt 0 ]; do
	case "$1" in
		"-check" )
			check_only=1
			shift
			;;
		"-yes" )
			yes="-y"
			shift
			;;
		"-without-java" | "-skip_java" )
			without_java=1
			shift
			;;
		"-without-python" | "-skip_python" )
			without_python=1
			shift
			;;
		"-with-conda" )
			with_conda=1
			without_python=1
			shift
			;;
		"-with-r" )
			with_r=1
			shift
			;;
		"-with-chrome" )
			with_chrome=1
			shift
			;;
		"-os" )
			[ $# -ge 3 ] || Usage
			distrib="$2"
			distribVersion="$3"
			shift 3
			;;
		*)
			Usage
			;;
	esac
done

#
# Identify OS distribution and check version
#
notSupported() {
	echo "*** OS distribution not supported *** $distrib $distribVersion" >&2
	exit 1
}

if [ -n "$distrib" ]; then
	echo "* Forcing OS distribution : $distrib $distribVersion"
else
	detectedDistrib=$("$(dirname "$0")"/../_find-distrib.sh)
	read distrib distribVersion <<< "$detectedDistrib"
	echo "+ Detected OS distribution : $distrib $distribVersion"
fi

case "$distrib" in
	debian)
		case "$distribVersion" in
			9* | 10*) ;;
			*) notSupported;;
		esac
		arch=$(dpkg --print-architecture)
		if [ "$arch" != "amd64" ]; then
			echo "*** Architecture not supported: '$arch' should be amd64" >&2
			exit 1
		fi
		;;
	ubuntu)
		case "$distribVersion" in
			1[6-9].* | 20.04) ;;
			*) notSupported;;
		esac
		arch=$(dpkg --print-architecture)
		if [ "$arch" != "amd64" ]; then
			echo "*** Architecture not supported: '$arch' should be amd64" >&2
			exit 1
		fi
		;;
	centos | redhat)
		major="$(echo "$distribVersion" | cut -d . -f 1)"
		minor="$(echo "$distribVersion" | cut -d . -s -f 2)"
		if [ -z "$minor" ]; then
			minor=0
		fi
		if [ "$major" = 6 -a "$minor" -ge 8 -o "$major" = 7 -o "$major" = 8 ]; then
			:
		else
			notSupported
		fi
		;;
	amazonlinux)
		case "$distribVersion" in
			201[78].* ) ;;
			2 ) ;;    # Amazon Linux 2
			*) notSupported;;
		esac
		;;
	oraclelinux)
		major="$(echo "$distribVersion" | cut -d . -f 1)"
		minor="$(echo "$distribVersion" | cut -d . -s -f 2)"
		if [ -z "$minor" ]; then
			minor=0
		fi
		if [ "$major" = 6 -a "$minor" -ge 8 -o "$major" = 7 -a "$minor" -ge 1 -o "$major" = 8 ]; then
			:
		else
			notSupported
		fi
		;;
	suse)
		case "$distribVersion" in
			12.[2-5] | 15 | 15.[12] ) ;;
			*) notSupported;;
		esac
		if [ -n "$yes" ]; then
			yes="-n"
		fi
		;;
	osx)
		major="$(echo "$distribVersion" | cut -d . -f 1)"
		minor="$(echo "$distribVersion" | cut -d . -f 2)"
		if [ "$major" -eq 10 -a "$minor" -ge 12 -o "$major" -eq 11 ]; then
			:
		else
			notSupported
		fi
		;;
	*)
		notSupported
		;;
esac

#
# Required packages
# Build list of required packages in PKG:
# - PKG_NAME
# - PKG_NAME>=MIN_VERSION     # Not implemented any more on RedHat
#
case "$distrib" in
	debian | ubuntu )
		PKG="
			acl
			curl
			git
			libexpat1
			libncurses5
			nginx
			unzip
			zip
		"
		if [ -z "$without_java" ]; then
			PKG+=" default-jre-headless"
		fi
		if [ -z "$without_python" ]; then
			PKG+=" python2.7 libpython2.7 libfreetype6 libgomp1"
			if [[ "$distrib" == "ubuntu" && "$distribVersion" == 18.* ]]; then
				PKG+=" python3.6 python3-distutils"
			else
				echo "* Warning : automatic installation of Python 3.6 not available on this platform"
				if [[ "$distrib" == "ubuntu" ]]; then
					echo "* See https://launchpad.net/~deadsnakes/+archive/ubuntu/ppa for a possible alternative installation source"
				fi
			fi
		fi
		if [ -n "$with_conda" ]; then
			# TBD : libgomp1 not required any more?
			PKG+=" bzip2 libgl1-mesa-glx libsm6 libxrender1 libgomp1 libasound2"
		fi
		if [ -n "$with_r" ]; then
			PKG+=" r-base-dev libicu-dev libcurl4-openssl-dev libssl-dev libxml2-dev pkg-config"
		fi
		if [ -n "$with_chrome" ]; then
			if [[ "$distrib" == "ubuntu" && "$distribVersion" == 20.* ]]; then
				PKG+=" npm"
			else
				# Use NodeJS from nodesource repository as:
				# - on Ubuntu 18.04, native version of NodeJS conflicts on libssl-dev (built on libssl1.0 instead of 1.1)
				# - on Debian 10, native version of npm does not support native version of nodejs
				# - on older releases, native version of nodejs is too old
				PKG+=" nodejs"
			fi
			PKG+=" libgtk-3-0 libnss3 libxss1 libasound2 libxtst6 libx11-xcb1"
		fi
		;;
	centos | redhat  | oraclelinux )
		PKG="
			acl
			expat
			git
			nginx
			unzip
			zip
		"
		if [[ "$distribVersion" == 8* ]]; then
			PKG+=" ncurses-compat-libs"
		fi
		if [ -z "$without_java" ]; then
			PKG+=" java-1.8.0-openjdk"
		fi
		if [ -z "$without_python" ]; then
			case "$distribVersion" in
				6*) PKG+=" python27 python36 freetype libgfortran libgomp";;
				7*) PKG+=" python3 freetype libgfortran libgomp";;
				8*) PKG+=" python2 python36 freetype libgfortran libgomp";;
			esac
		fi
		if [ -n "$with_conda" ]; then
			PKG+=" bzip2 mesa-libGL libSM libXrender libgomp alsa-lib"
		fi
		if [ -n "$with_r" ]; then
			PKG+=" R-core-devel libicu-devel libcurl-devel openssl-devel libxml2-devel"
		fi
		if [ -n "$with_chrome" ]; then
			if [[ "$distribVersion" == 6* ]]; then
				echo >&2 "*** -with-chrome not supported on this distribution: $distrib $distribVersion"
				exit 1
			fi
			PKG+=" npm gtk3 libXScrnSaver alsa-lib"
		fi
		;;
	amazonlinux )
		PKG="
			acl
			expat
			git
			nginx
			unzip
			zip
		"
		if [ "$distribVersion" = 2 ]; then
			PKG+=" ncurses-compat-libs"
		fi
		if [ -z "$without_java" ]; then
			PKG+=" java-1.8.0-openjdk"
		fi
		if [ -z "$without_python" ]; then
			if [ "$distribVersion" = 2 ]; then
				PKG+=" python freetype compat-gcc-48-libgfortran libgomp"
				echo "* Warning : automatic installation of Python 3.6 not available on this platform"
			elif [ "$distribVersion" = 2017.03 ]; then
				# Python 3.6 is not available in 2017.03
				PKG+=" python27 freetype libgfortran libgomp"
				echo "* Warning : automatic installation of Python 3.6 not available on this platform"
			else
				PKG+=" python27 python36 freetype libgfortran libgomp"
			fi
		fi
		if [ -n "$with_conda" ]; then
			PKG+=" bzip2 mesa-libGL libSM libXrender libgomp"
		fi
		if [ -n "$with_r" ]; then
			PKG+=" R-core-devel libicu-devel libcurl-devel openssl-devel libxml2-devel"
		fi
		if [ -n "$with_chrome" ]; then
			if [ "$distribVersion" = 2 ]; then
				PKG+=" npm gtk3 libXScrnSaver alsa-lib"
			else
				echo >&2 "*** -with-chrome not supported on this distribution: $distrib $distribVersion"
				exit 1
			fi
		fi
		;;
	suse )
		PKG="
			acl
			git-core
			libexpat1
			libncurses5
			nginx
			unzip
			zip
		"
		if [ -z "$without_java" ]; then
			PKG+=" java-1_8_0-openjdk-headless"
		fi
		if [ -z "$without_python" ]; then
			PKG+=" python python-xml libfreetype6 libgomp1"
			if [[ "$distribVersion" == 15* ]]; then
				PKG+=" python3"
			else
				PKG+=" libgfortran3"
				if [[ "$distribVersion" == "12.5" ]]; then
					PKG+=" python36"
				else
					echo "* Warning : automatic installation of Python 3.6 not available on this platform"
				fi
			fi
		fi
		if [ -n "$with_conda" ]; then
			PKG+=" bzip2 Mesa-libGL1 libSM6 libXrender1 libgomp1"
		fi
		if [ -n "$with_r" ]; then
			if [[ "$distribVersion" == 15* ]]; then
				PKG+=" patterns-devel-base-devel_basis"
			else
				PKG+=" patterns-sles-Basis-Devel"
			fi
			PKG+=" gcc-fortran R-core-devel libicu-devel libcurl-devel libopenssl-devel libxml2-devel"
		fi
		if [ -n "$with_chrome" ]; then
			PKG+=" npm10 libgtk-3-0 mozilla-nss libXss1 libasound2"
		fi
		;;
esac

#
# Check whether a service is configured to start at boot
#
is_service_enabled() {
	if command -v systemctl >/dev/null; then
		if systemctl list-unit-files "$1.service" | grep -q "^$1\\.service" &&
		   systemctl is-enabled --quiet "$1" 2>/dev/null; then
			echo "enabled"
		fi
	elif command -v chkconfig >/dev/null; then
		if chkconfig "$1"; then
			echo "enabled"
		fi
	else
		# Legacy mode for stripped-down Debian-based systems
		if ls /etc/rc?.d/S*"$1" >/dev/null 2>&1; then
			echo "enabled"
		fi
	fi
}

#
# Disable a boot-time service
#
disable_service() {
	if command -v systemctl >/dev/null; then
		systemctl disable "$1"
	elif command -v chkconfig >/dev/null; then
		chkconfig "$1" off
	else
		update-rc.d "$1" disable
	fi
}


#
# Install required packages
#
install_deps() {
	echo "+ Checking required repositories..."

	case "$distrib" in
		debian )
			apt_update=
			case "$distribVersion" in
				9*)
					if [ -n "$with_r" ] && ! grep -qs -e cloud.r-project.org /etc/apt/sources.list /etc/apt/sources.list.d/*.list; then
						echo "+ Adding CRAN repository for R ..."
						[ -f /usr/bin/add-apt-repository ] ||
							apt-get $yes install software-properties-common
						[ -f /usr/bin/dirmngr ] ||
							apt-get $yes install dirmngr
						apt-key adv --keyserver keys.gnupg.net --recv-key E19F5F87128899B192B1A2C2AD5F960A256A04AF
						add-apt-repository $yes 'deb http://cloud.r-project.org/bin/linux/debian stretch-cran35/'
						apt_update=1
					fi
					if [ -n "$with_chrome" ] && ! grep -qs deb.nodesource.com /etc/apt/sources.list /etc/apt/sources.list.d/*.list; then
						echo "+ Adding nodesource repository for nodejs ..."
						[ -f /usr/bin/add-apt-repository ] ||
							apt-get $yes install software-properties-common
						[ -f /usr/bin/dirmngr ] ||
							apt-get $yes install dirmngr
						apt-key adv --fetch-keys https://deb.nodesource.com/gpgkey/nodesource.gpg.key
						add-apt-repository $yes 'deb http://deb.nodesource.com/node_8.x stretch main'
						apt_update=1
					fi
					;;
				10*)
					if [ -n "$with_chrome" ] && ! grep -qs deb.nodesource.com /etc/apt/sources.list /etc/apt/sources.list.d/*.list; then
						echo "+ Adding nodesource repository for nodejs ..."
						[ -f /usr/bin/add-apt-repository ] ||
							apt-get $yes install software-properties-common
						[ -f /usr/bin/dirmngr ] ||
							apt-get $yes install dirmngr
						apt-key adv --fetch-keys https://deb.nodesource.com/gpgkey/nodesource.gpg.key
						add-apt-repository $yes 'deb http://deb.nodesource.com/node_10.x buster main'
						apt_update=1
					fi
					;;
			esac
			if [ -n "$apt_update" ]; then
				apt-get $yes update
				apt_update=
			fi
			;;

		ubuntu )
			apt_update=
			case "$distribVersion" in
				16.04)
					if [ -n "$with_r" ] && ! grep -qs -e cloud.r-project.org /etc/apt/sources.list /etc/apt/sources.list.d/*.list; then
						echo "+ Adding CRAN repository for R ..."
						[ -f /usr/bin/add-apt-repository ] ||
							apt-get $yes install software-properties-common
						apt-key adv --keyserver keyserver.ubuntu.com --recv-key E298A3A825C0D65DFD57CBB651716619E084DAB9
						add-apt-repository $yes 'deb http://cloud.r-project.org/bin/linux/ubuntu xenial-cran35/'
						apt_update=1
					fi
					if [ -n "$with_chrome" ] && ! grep -qs deb.nodesource.com /etc/apt/sources.list /etc/apt/sources.list.d/*.list; then
						echo "+ Adding nodesource repository for nodejs ..."
						[ -f /usr/bin/add-apt-repository ] ||
							apt-get $yes install software-properties-common
						apt-key adv --keyserver keyserver.ubuntu.com --recv-key 68576280
						add-apt-repository $yes 'deb http://deb.nodesource.com/node_8.x xenial main'
						apt_update=1
					fi
					;;
				18.*)
					if [ -n "$with_r" ] && ! grep -qs -e cloud.r-project.org /etc/apt/sources.list /etc/apt/sources.list.d/*.list; then
						echo "+ Adding CRAN repository for R ..."
						[ -f /usr/bin/add-apt-repository ] ||
							apt-get $yes install software-properties-common
						apt-key adv --keyserver-options http-proxy={{proxy}} --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys E298A3A825C0D65DFD57CBB651716619E084DAB9
						case "$distribVersion" in
							18.04) add-apt-repository $yes 'deb https://cloud.r-project.org/bin/linux/ubuntu bionic-cran35/';;
							18.10) add-apt-repository $yes 'deb https://cloud.r-project.org/bin/linux/ubuntu cosmic-cran35/';;
						esac
						apt_update=1
					fi
					# Switch to nodesource repository for nodejs as the default Ubuntu 18.* build of npm
					# is built on libssl1.0 and conflicts with libssl-dev
					if [ -n "$with_chrome" ] && ! grep -qs deb.nodesource.com /etc/apt/sources.list /etc/apt/sources.list.d/*.list; then
						echo "+ Adding nodesource repository for nodejs ..."
						[ -f /usr/bin/add-apt-repository ] ||
							apt-get $yes install software-properties-common
						apt-key adv --fetch-keys https://deb.nodesource.com/gpgkey/nodesource.gpg.key
						case "$distribVersion" in
							18.04) add-apt-repository $yes 'deb https://deb.nodesource.com/node_10.x bionic main';;
							18.10) add-apt-repository $yes 'deb https://deb.nodesource.com/node_10.x cosmic main';;
						esac
						apt_update=1
					fi
					;;
			esac
			if [ -n "$apt_update" ]; then
				apt-get $yes update
				apt_update=
			fi
			;;

		centos | redhat | oraclelinux )
			repolist=$(yum repolist)
			repos=
			# EPEL is required for nginx (except on v8) and R if required
			if [[ "$distribVersion" != 8* || -n "$with_r" ]]; then
				grep -qE '^[!*]?epel[[:space:]/]' <<< "$repolist" || {
					echo "+ Adding EPEL repository ..."
					if [ "$distrib" = "centos" ]; then
						repos+=" epel-release"
					else
						case "$distribVersion" in
							6*) repos+=" http://dl.fedoraproject.org/pub/epel/epel-release-latest-6.noarch.rpm";;
							7*) repos+=" http://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm";;
							8*) repos+=" http://dl.fedoraproject.org/pub/epel/epel-release-latest-8.noarch.rpm";;
						esac
					fi
				}
			fi
			# RH/Centos/OL 6 requires IUS for Python2.7 and Python3.6
			if [[ "$distribVersion" == 6* && -z "$without_python" ]]; then
				grep -qE '^[!*]?ius[[:space:]/]' <<< "$repolist" || {
					echo "+ Adding IUS repository for python 2.7 and python 3.6..."
					repos+=" https://repo.ius.io/ius-release-el6.rpm"
				}
			fi
			# RH/Centos/OL 6 requires nginx from the nginx.org repo, as the EPEL version is missing the http_auth_request module
			if [[ "$distribVersion" == 6* ]]; then
				grep -qE '^[!*]?nginx[[:space:]/]' <<< "$repolist" || {
					echo "+ Adding nginx repository ..."
					case "$distrib" in
						redhat) repos+=" https://nginx.org/packages/rhel/6/noarch/RPMS/nginx-release-rhel-6-0.el6.ngx.noarch.rpm";;
						*) repos+=" https://nginx.org/packages/centos/6/noarch/RPMS/nginx-release-centos-6-0.el6.ngx.noarch.rpm";;
					esac
				}
			fi
			if [ "$distrib" = "redhat" ] && grep -q epel-release <<< "$repos"; then
				# On RedHat, EPEL packages may have dependencies in "optional" repo, which is not enabled by default
				if [[ "$distribVersion" != 8* ]]; then
					echo "* Warning: you may need to enable the RedHat 'optional' repository in case of missing dependencies"
					echo "* See https://fedoraproject.org/wiki/EPEL#How_can_I_use_these_extra_packages.3F"
				else
					echo "* Warning: you may need to enable the RedHat 'codeready-builder-for-rhel-8' repository in case of missing dependencies"
					echo "* See https://fedoraproject.org/wiki/EPEL#How_can_I_use_these_extra_packages.3F"
				fi
			fi
			if [ -n "$repos" ]; then
				yum install -q $yes $repos
			fi
			;;

		amazonlinux )
			if [ "$distribVersion" = "2" ]; then
				if ! rpm --quiet -q nginx; then
					echo "+ Enabling nginx1 topic..."
					amazon-linux-extras enable nginx1
				fi
				if [ -n "$with_chrome" ] && ! rpm --quiet -q epel-release; then
					# Use EPEL for nodejs
					echo "+ Adding EPEL repository ..."
					amazon-linux-extras enable epel
					yum install -q $yes epel-release
				fi
				if [ -n "$with_r" ] && ! rpm --quiet -q R-core; then
					echo "+ Enabling R3.4 topic..."
					amazon-linux-extras enable R3.4
				fi
			fi
			;;

		suse )
			zypper repos -u | grep -q nginx || {
				echo "+ Adding nginx repository for nginx stable version..."
				zypper $yes addrepo -n "nginx stable" 'https://nginx.org/packages/sles/$releasever_major' nginx-stable
				rpm --import https://nginx.org/keys/nginx_signing.key
			}
			if [ -n "$with_r" ]; then
				zypper repos -u | grep -q "devel:/languages:/R:" || {
					echo "+ Adding devel:languages:R:patched repository for R ..."
					case "$distribVersion" in
						12*) zypper $yes addrepo -n "R patched" obs://devel:languages:R:patched/SLE_12 R_patched;;
						15) zypper $yes addrepo -n "R patched" obs://devel:languages:R:patched/SLE_15 R_patched;;
						15.*) zypper $yes addrepo -n "R patched" obs://devel:languages:R:patched/SLE_15_SP1 R_patched;;
					esac
				}
			fi
			;;
	esac

	echo "+ Installing required packages..."

	# Check whether nginx is already configured to start on boot
	# as installing the package may unnecessary set it
	nginx_enabled="$(is_service_enabled nginx)"

	case "$distrib" in
		debian | ubuntu )
			pkglist=		
			for pkg in $PKG; do
				case "$pkg" in
					*'>='*)
						minver=$(echo "$pkg" | awk -F '>=' '{print $2}')
						pkg=$(echo "$pkg" | awk -F '>=' '{print $1}')
						;;
					*)
						minver=
						;;
				esac
				pkglist+=" $pkg"
			done
			apt-get $yes install $pkglist
			;;

		centos | redhat | amazonlinux | oraclelinux )
			pkglist=
			enablerepo=
			for pkg in $PKG; do
				pkglist+=" $pkg"
				# R-core-devel may require enabling additional repositories
				if [ "$pkg" = "R-core-devel" ] && ! rpm --quiet -q R-core-devel; then
					if [[ "$distrib" == "centos" && "$distribVersion" == 8* ]]; then
						echo "+ Enabling PowerTools repository for R dependencies..."
						enablerepo="--enablerepo=PowerTools"
					elif [[ "$distrib" == "redhat" && "$distribVersion" == 8* ]]; then
						echo "**** WARNING ****"
						echo "* Some dependencies for R-core-devel are in the codeready-builder-for-rhel-8 repository"
						echo "* which may not be enabled on some RHEL 8 systems"
						echo "* In case of missing dependencies, make sure to enable this repository"
						echo "* using 'subscription-manager'"
						echo "*"
						echo "* You can also install this package separately with:"
						echo "*    sudo yum install R-core-devel --enablerepo=CODEREADY_REPO"
						echo "* where CODEREADY_REPO is the name of this repository on your server"
						echo "* which you can retrieve with:"
						echo "*     sudo yum repolist --all"
						echo "*****************"
					elif [[ "$distrib" == "redhat" ]]; then
						echo "**** WARNING ****"
						echo "* Some dependencies for R-core-devel are in the 'optional' RedHat repository"
						echo "* which may not be enabled on some RHEL systems"
						echo "* In case of missing dependencies, make sure to enable this repository"
						echo "* using 'subscription-manager' or 'yum-config-manager'"
						echo "*"
						echo "* You can also install this package separately with:"
						echo "*    sudo yum install R-core-devel --enablerepo=OPTIONAL_REPO"
						echo "* where OPTIONAL_REPO is the name of this repository on your server"
						echo "* which you can retrieve with:"
						echo "*     sudo yum repolist all"
						echo "*****************"
					elif [[ "$distrib" == "oraclelinux" && "$distribVersion" == 7* ]]; then
						echo "+ Enabling OL7 optional repository for R dependencies..."
						enablerepo="--enablerepo=ol7_optional_latest"
					elif [[ "$distrib" == "oraclelinux" && "$distribVersion" == 8* ]]; then
						echo "+ Enabling CodeReady Builder repository for R dependencies..."
						enablerepo="--enablerepo=ol8_codeready_builder"
					fi
				elif [[ "$distribVersion" == 6* && "$pkg" == "python27" ]] && ! rpm --quiet -q python27; then
					# Python 2.7 has been moved to the "archive" section of IUS
					echo "+ Enabling ius-archive repository for Python 2.7..."
					enablerepo="--enablerepo=ius-archive"
				fi
			done
			yum $yes install $enablerepo $pkglist
			;;

		suse )
			pkglist=
			for pkg in $PKG; do
				pkglist+=" $pkg"
			done
			zypper $yes install $pkglist
			;;
	esac

	if [ -z "$nginx_enabled" -a -n "$(is_service_enabled nginx)" ]; then
		echo "+ Disabling nginx service"
		disable_service nginx
	fi
}


#
# Check package minimum version
# checkMin{Deb,Rpm}Version VERSION MIN_VERSION
#
checkMinDebVersion() {
	dpkg --compare-versions "$1" ge "$2"
}

# Not valid any more under RH8 as it depends on the Yum-3 API
# Could be done with /usr/libexec/platform-python and the dnf API?
checkMinRpmVersion() {
	/usr/bin/python -c '
import sys, rpm
from rpmUtils.miscutils import stringToVersion
(e1, v1, r1) = stringToVersion(sys.argv[1])
(e2, v2, r2) = stringToVersion(sys.argv[2])
if rpm.labelCompare(("0", v1, None), ("0", v2, None)) < 0:
	sys.exit(1)
' "$1" "$2"
}

#
# Check required packages
#
check_deps() {
	err=0

	echo "+ Checking required packages..."
	case "$distrib" in
		debian | ubuntu )
		for pkg in $PKG; do
			case "$pkg" in
				*'>='*)
					minver=$(echo "$pkg" | awk -F '>=' '{print $2}')
					pkg=$(echo "$pkg" | awk -F '>=' '{print $1}')
					;;
				*)
					minver=
					;;
			esac
			if [ "$(dpkg-query -W -f='${Status}\n' $pkg:amd64 2>/dev/null)" = "install ok installed" ]; then
				arch=amd64
			elif [ "$(dpkg-query -W -f='${Status}\n' $pkg:all 2>/dev/null)" = "install ok installed" ]; then
				arch=all
			else
				echo "*** Error: package $pkg not found" >&2
				err=1
				minver=
			fi
			if [ -n "$minver" ]; then
				pkgver=$(dpkg-query -W -f='${Version}\n' $pkg:$arch 2>/dev/null)
				checkMinDebVersion "$pkgver" "$minver" || {
					echo "*** Error: package $pkg has version $pkgver should be >= $minver" >&2
					err=1
				}
			fi
		done
		;;

		centos | redhat | amazonlinux | oraclelinux )
		for pkg in $PKG; do
			rpm --quiet -q "$pkg" || {
				echo "*** Error: package $pkg not found" >&2
				err=1
				minver=
			}
		done
		;;

		suse )
		for pkg in $PKG; do
			rpm -q "$pkg" >&/dev/null || {
				echo "*** Error: package $pkg not found" >&2
				err=1
			}
		done
		;;
	esac

	return $err
}

#
# Main entry point
#
if [ -n "$check_only" ]; then
	check_deps
else
	install_deps
fi
