#!/bin/bash
###############################################
#Run the from the parent Directory
#./utils/gitFileOverride/fileOverride.sh
#Otherwise, it will throw error file not found
###############################################

csv_file_overwrite_info='./utils/gitFileOverride/fileOverrideList.csv'

if [ -z "$csv_file_overwrite_info" ]
then
      echo "variable \$csv_file_overwrite_info is unset"
      exit 1
fi

line_number=1
sed -i 's/\r//g' $csv_file_overwrite_info

while IFS=';' read -r src dest
do
    if [ "$line_number" == '1' ]
    then
        echo "Skipping the header"
        line_number=$[$line_number +1]
        continue
    fi
    
    if [ "$line_number" == '2' ]
    then
        echo "Start Override"
        echo "========================"
        echo "Source  -> Destination"
        echo "========================"
    fi

    if [ "$src" == "" ]
    then
        echo "Line number $line_number: Source value is empty or no value set"
    elif [ "$dest" == "" ]
    then
        echo "Line number $line_number: Destination value is empty or no value set"
    else
        mkdir -p $dest && cp -vr $src $_
    fi

    line_number=$[$line_number +1]

done < <(grep "" $csv_file_overwrite_info)

