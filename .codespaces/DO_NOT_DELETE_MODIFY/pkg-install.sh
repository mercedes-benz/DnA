if [ -d "$HOME/.sdkman" ]; then
    echo "SDKMAN! is already installed."
else
    echo "Installing SDKMAN for $SHORTID on Workspace $HOSTNAME"
    curl -s --insecure "https://get.sdkman.io" | bash
  if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
      chmod +x $HOME/.sdkman/bin/sdkman-init.sh
      source "$HOME/.sdkman/bin/sdkman-init.sh"
      echo "SDKMAN! installed."
  else
      echo "SDKMAN! installation failed."
  fi
fi
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.11-amzn
code-server --install-extension vscjava.vscode-java-pack
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install gradle 7.6.3

code-server --install-extension mtxr.sqltools-driver-pg
code-server --install-extension mtxr.sqltools
code-server --install-extension cweijan.vscode-database-client2
code-server --install-extension cweijan.vscode-redis-client
