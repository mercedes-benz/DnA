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
sdk install java 17.0.12-ms
code-server --install-extension vscjava.vscode-java-pack
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install gradle 7.2


curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"  # This loads nvm
[ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion"  # This loads nvm bash_completion
nvm install 22.11.0
npm install -g yarn@1.22.22

code-server --install-extension christian-kohler.npm-intellisense
code-server --install-extension mtxr.sqltools-driver-pg
code-server --install-extension mtxr.sqltools
code-server --install-extension cweijan.vscode-database-client2
code-server --install-extension cweijan.vscode-redis-client
