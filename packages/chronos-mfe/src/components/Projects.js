import classNames from 'classnames';
import React, { useEffect, useState } from 'react';
import Styles from './Projects.style.scss';
import { useDispatch, useSelector } from 'react-redux';
import { useForm, FormProvider } from 'react-hook-form';

// import from DNA Container
import Pagination from 'dna-container/Pagination';
import Modal from 'dna-container/Modal';

import { setProjects, setPagination } from './redux/projectsSlice';
import { GetProjects } from './redux/projects.services';
import ProjectsCardItem from './ProjectCardItem';

const ForeCastingProjects = () => {
  const [createProject, setCreateProject] = useState(false);
  const [editProject, setEditProject] = useState(false);

  const methods = useForm();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = methods;

  const dispatch = useDispatch();
  const {
    projects,
    pagination: { projectListResponse, totalNumberOfPages, currentPageNumber, maxItemsPerPage },
  } = useSelector((state) => state.projects);

  useEffect(() => {
    dispatch(GetProjects());
  }, [dispatch]);

  const onPaginationPreviousClick = () => {
    const currentPageNumberTemp = currentPageNumber - 1;
    const currentPageOffset = (currentPageNumberTemp - 1) * maxItemsPerPage;
    const modifiedData = projectListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch(setProjects(modifiedData));
    dispatch(setPagination({ currentPageNumber: currentPageNumberTemp }));
  };
  const onPaginationNextClick = () => {
    let currentPageNumberTemp = currentPageNumber;
    const currentPageOffset = currentPageNumber * maxItemsPerPage;
    currentPageNumberTemp = currentPageNumber + 1;
    const modifiedData = projectListResponse.slice(currentPageOffset, maxItemsPerPage * currentPageNumberTemp);
    dispatch(setProjects(modifiedData));
    dispatch(setPagination({ currentPageNumber: currentPageNumberTemp }));
  };
  const onViewByPageNum = (pageNum) => {
    const totalNumberOfPages = Math.ceil(projectListResponse?.length / pageNum);
    const modifiedData = projectListResponse.slice(0, pageNum);
    dispatch(setProjects(modifiedData));
    dispatch(
      setPagination({
        totalNumberOfPages,
        maxItemsPerPage: pageNum,
        currentPageNumber: 1,
      }),
    );
  };

  const handleCreateProject = (values) => {
    values.permission = values.permission.reduce((acc, curr) => {
      acc[curr] = true;
      return acc;
    }, {});
    // build mock data
    const names = ['John Doe', 'Josip Skafar'];
    const [firstName, lastName] = names[Math.floor(Math.random() * names.length)].split(' ');
    values.createdDate = new Date().toISOString();
    values.createdBy = {
      firstName,
      lastName,
    };
    values.id = Math.floor(Math.random() * 10);
    dispatch(setProjects([...projects, values]));
    setCreateProject(false);
    reset({ name: '' });
  };
  const handleEditProject = (values) => {
    values.permission = values.permission.reduce((acc, curr) => {
      acc[curr] = true;
      return acc;
    }, {});
    const copyProjects = [...projects];
    const findIndex = copyProjects.findIndex((item) => item.id === values.id);
    copyProjects[findIndex] = values;
    dispatch(setProjects(copyProjects));
    setEditProject(false);
    reset({ name: '' });
  };

  const addProjectContent = (
    <FormProvider {...methods}>
      <div className={Styles.content}>
        <div className={Styles.formGroup}>
          <div className={Styles.flexLayout}>
            <div>
              <div className={classNames('input-field-group include-error', errors?.name ? 'error' : '')}>
                <label className={classNames(Styles.inputLabel, 'input-label')}>
                  Name of Project <sup>*</sup>
                </label>
                <div>
                  <input
                    type="text"
                    className={classNames('input-field', Styles.projectNameField)}
                    id="projectName"
                    placeholder="Type here"
                    autoComplete="off"
                    {...register('name', { required: '*Missing entry' })}
                  />
                  <span className={classNames('error-message')}>{errors?.name?.message}</span>
                </div>
              </div>
            </div>
            <div className={Styles.flexLayout}>
              <div>
                <div className={classNames('input-field-group include-error')}>
                  <label className={classNames(Styles.inputLabel, 'input-label')}>
                    Permission <sup>*</sup>
                  </label>
                  <div className={Styles.permissionField}>
                    <div className={Styles.checkboxContainer}>
                      <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                        <span className="wrapper">
                          <input
                            name="read"
                            type="checkbox"
                            className="ff-only"
                            {...register('permission')}
                            value="read"
                            checked={true}
                          />
                        </span>
                        <span className="label">Read</span>
                      </label>
                    </div>
                    <div className={Styles.checkboxContainer}>
                      <label className={classNames('checkbox', Styles.checkBoxDisable)}>
                        <span className="wrapper">
                          <input
                            name="write"
                            type="checkbox"
                            className="ff-only"
                            {...register('permission')}
                            value="write"
                            checked={true}
                          />
                        </span>
                        <span className="label">Write</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className={Styles.btnContainer}>
            <button
              className="btn btn-tertiary"
              type="button"
              onClick={handleSubmit((values) => {
                createProject ? handleCreateProject(values) : handleEditProject(values);
              })}
            >
              {createProject ? 'Create Project' : editProject && 'Save Project'}
            </button>
          </div>
        </div>
      </div>
    </FormProvider>
  );

  return (
    <>
      <div className={classNames(Styles.mainPanel)}>
        <div className={classNames(Styles.wrapper)}>
          <div className={classNames(Styles.caption)}>
            <h3>My Forecasting Projects</h3>
            <div className={classNames(Styles.listHeader)}>
              {projects?.length ? (
                <React.Fragment>
                  <button
                    className={projects?.length === null ? Styles.btnHide : 'btn btn-primary'}
                    type="button"
                    onClick={() => setCreateProject(true)}
                  >
                    <i className="icon mbc-icon plus" />
                    <span>Provide Forecasting Project</span>
                  </button>
                </React.Fragment>
              ) : null}
            </div>
          </div>
          <div>
            <div>
              {projects?.length === 0 ? (
                <div className={classNames(Styles.content)}>
                  <div className={Styles.listContent}>
                    <div className={Styles.emptyProducts}>
                      <span>
                        You don&apos;t have any forecasting projects at this time.
                        <br /> Please create a new one.
                      </span>
                    </div>
                    <div className={Styles.subscriptionListEmpty}>
                      <br />
                      <button className={'btn btn-tertiary'} type="button" onClick={() => setCreateProject(true)}>
                        <span>Provide Forecasting Project</span>
                      </button>
                    </div>
                  </div>
                </div>
              ) : (
                <>
                  <div className={Styles.allProjectContent}>
                    <div className={classNames('cardSolutions', Styles.allProjectCardviewContent)}>
                      {projects?.map((project, index) => {
                        return (
                          <ProjectsCardItem
                            key={index}
                            project={project}
                            onEdit={(val) => {
                              setEditProject(true);
                              reset(val);
                            }}
                          />
                        );
                      })}
                    </div>
                  </div>
                  {projects?.length ? (
                    <Pagination
                      totalPages={totalNumberOfPages}
                      pageNumber={currentPageNumber}
                      onPreviousClick={onPaginationPreviousClick}
                      onNextClick={onPaginationNextClick}
                      onViewByNumbers={onViewByPageNum}
                      displayByPage={true}
                    />
                  ) : null}
                </>
              )}
            </div>
          </div>
        </div>
      </div>
      <Modal
        title={createProject ? 'Create new forecasting project' : editProject && 'Edit forecasting project'}
        showAcceptButton={false}
        showCancelButton={false}
        modalWidth={'60%'}
        buttonAlignment="right"
        show={createProject || editProject}
        content={addProjectContent}
        scrollableContent={false}
        onCancel={() => {
          setCreateProject(false);
          setEditProject(false);
        }}
      />
    </>
  );
};
export default ForeCastingProjects;
