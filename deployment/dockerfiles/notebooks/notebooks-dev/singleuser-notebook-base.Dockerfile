FROM jupyter/pyspark-notebook:ubuntu-18.04

USER root

COPY ./extensions/overrides/exercise2/main.css /opt/conda/share/jupyter/nbextensions/exercise2/main.css
COPY ./extensions/overrides/exercise2/main.js /opt/conda/share/jupyter/nbextensions/exercise2/main.js
COPY ./gamification/users/assets/congrats.jpeg /tmp/assets/congrats.jpeg
COPY ./gamification/users/ParticipantNotebook.ipynb /tmp/ITSMM/ParticipantNotebook.ipynb

ADD jupyter_notebook_config.py /etc/jupyter/jupyter_notebook_config.py

# Install basic dependencies
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        ca-certificates bash-completion tar less && \
    rm -rf /var/lib/apt/lists/*

RUN pip install jupyter_contrib_nbextensions
RUN jupyter contrib nbextension install --sys-prefix
RUN jupyter nbextension enable rubberband/main --sys-prefix
RUN jupyter nbextension enable exercise2/main --sys-prefix

# Install latest KFP SDK
RUN pip3 freeze
RUN pip3 install --upgrade pip && \
    # XXX: Install enum34==1.1.8 because other versions lead to errors during
    #  KFP installation
    pip3 install --upgrade "enum34==1.1.8" && \
    pip3 install jupyterlab-gitlab==2.0.0 && \
    pip3 install --upgrade "jupyterlab>=2.0.0,<3.0.0"

# Install Kale from KALE_BRANCH (defaults to "master")
ARG KALE_BRANCH="master"
WORKDIR /
RUN git config --global http.sslverify false
RUN git clone -b ${KALE_BRANCH} https://github.com/kubeflow-kale/kale

WORKDIR /kale/backend
COPY kale/backend/kale/templates/pipeline_template.jinja2 ./kale/templates/
COPY kale/backend/kale/templates/nb_function_template.jinja2 ./kale/templates/
COPY kale/backend/kale/compiler.py ./kale/compiler.py
COPY kale/backend/kale/common/podutils.py ./kale/common/podutils.py
COPY kale/backend/kale/common/serveutils.py ./kale/common/serveutils.py

RUN sudo chmod -R 755 /tmp
RUN sudo chmod -R 755 ./kale/templates/
RUN sudo chmod -R 755 ./kale

RUN pip3 install --upgrade .

WORKDIR /kale/labextension
COPY kale/labextension/src/widgets/deploys-progress/DeployProgress.txt ./src/widgets/deploys-progress/DeployProgress.tsx

RUN npm config set strict-ssl false && \
    npm install --global yarn && \
    yarn config set "strict-ssl" false && \
    jlpm install && \
    jlpm run build && \
    jupyter labextension install .

RUN jupyter labextension install @jupyterlab/git
RUN pip3 install jupyterlab-git==0.24.0
RUN pip install nbgitpuller 
RUN jupyter lab build

RUN chmod 1777 /tmp
RUN mkdir /marshal
RUN chmod a=rwx,u+t /marshal
RUN chown -R 1000:1000 /marshal

WORKDIR "${HOME}"

RUN mkdir ./.kale.kfserving.model.dir
RUN chmod a=rwx,u+t ./.kale.kfserving.model.dir
RUN chown -R 8737:8737 ./.kale.kfserving.model.dir

USER ${NB_UID}