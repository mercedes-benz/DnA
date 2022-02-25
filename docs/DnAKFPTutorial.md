# How to convert a Jupyter Notebook into a Kubeflow Pipeline

*The DnA platform offers a holistic open-source [MLOps pipeline](https://github.com/mercedes-benz/DnA/blob/kubeflow-tutorial-pipelines/docs/DnAMLOPsArchitecture.md) utilizing existing FOSS solutions. This tutorial demonstrates the three simple steps required for the conversion of a Jupyter Notebook into a [Kubeflow Pipeline](https://github.com/kubeflow/pipelines) using [Kale](https://github.com/kubeflow-kale/kale) directly from the DnA Platform enviroment.*

**Step 1**:

Users can launch their own Jupyter Notebooks instance by clicking on the "My Workspaces" tab.

<img alt="DnA - Launching a Jupyter Notebook instance" src="/docs/images/DnANotebooks.gif" style="max-width:100%">

**Step 2**:

To convert a Jupyter Notebook into a Kubeflow Pipeline, users should enable the Kale JupyterLab extension. Then all they have to do is, annotate the different cells of the Notebook, define their interdependencies, specify an experiment and a pipeline name, and finally click on the "COMPILE AND RUN" button.

<img alt="DnA - Using Kale" src="/docs/images/DnAKale.gif" style="max-width:100%">

**Step 3**:

The resulted pipeline can be accessed by clicking on the link, as shown below.

<img alt="DnA - Accessing the resulted KFP" src="/docs/images/DnAKFP.gif" style="max-width:100%">


**Additional Info**

To explore the benefits of the Kubeflow Pipelines, please visit the official [Kubeflow Documentation](https://www.kubeflow.org/docs/components/pipelines/).

To explore the benefits of Kale, please visit the official [Kale Documentation](https://www.kubeflow.org/docs/external-add-ons/kale/).
