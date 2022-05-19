from pathlib import Path
from setuptools import setup, find_packages


def read_requirements(path):
    return list(Path(path).read_text().splitlines())


setup(
    name="PythonTemplate",
    description='Unit8 python library template.',
    version="dev",
    python_requires='>=3.6',
    install_requires=read_requirements('requirements/main.txt'),
    packages=find_packages(),
)