\c dashboard;

INSERT INTO common_function_sql (id, name) VALUES
(DEFAULT, 'CommonFunction 1'),
(DEFAULT,'CommonFunction 2');

INSERT INTO specific_function_sql (id, name) VALUES
(DEFAULT, 'SpecificFunction 1'),
(DEFAULT,'SpecificFunction 2');

INSERT INTO integrated_portal_sql (id, name) VALUES
(DEFAULT, 'integrated_portal1'),
(DEFAULT,'integrated_portal2');

INSERT INTO frontend_technology_sql (id, name) VALUES
(DEFAULT, 'Power BI'),
(DEFAULT,'Tableau'),
(DEFAULT,'Lumira Design'),
(DEFAULT,'Customized Application'),
(DEFAULT,'FIORI');

INSERT INTO hierarchy_sql (id, name) VALUES
(DEFAULT, 'Top Mangement FC (E1/E2)'),
(DEFAULT,'Mangement FC (E3/E4)'),
(DEFAULT, 'Top Management MBC (E1/E2)'),
(DEFAULT, 'Management MBC (E3/E4');

INSERT INTO ressort_sql (id, name) VALUES
(DEFAULT, 'FMB'),
(DEFAULT,'FMC'),
(DEFAULT,'FMS'),
(DEFAULT,'FMP'),
(DEFAULT,'FMZ'),
(DEFAULT,'FMA');

INSERT INTO customer_department_sql (id, name) VALUES
(DEFAULT, 'GL'),
(DEFAULT,'BF'),
(DEFAULT,'BL'),
(DEFAULT,'C');

INSERT INTO kpi_name_sql (id, name) VALUES
(DEFAULT, 'Contribution Margin per Unit'),
(DEFAULT, 'CAV'),
(DEFAULT, 'ROI'),
(DEFAULT, 'Cost per Acquisition'),
(DEFAULT, 'EBIT');

INSERT INTO reporting_cause_sql (id, name) VALUES
(DEFAULT, 'IST'),
(DEFAULT,'Plan');


INSERT INTO product_phase_sql (id, name) VALUES
(DEFAULT, 'product_phase1'),
(DEFAULT,'product_phase2');

INSERT INTO query_sql (id, name) VALUES
(DEFAULT, 'Query 1'),
(DEFAULT,'Query 2'),
(DEFAULT,'Query 3');

INSERT INTO status_sql (id, name) VALUES
(DEFAULT, 'Live'),
(DEFAULT,'In Progress'),
(DEFAULT,'Idea');

INSERT INTO design_guide_sql (id, name) VALUES
(DEFAULT, 'design_guide1'),
(DEFAULT,'design_guide2');

INSERT INTO agile_releasetrain_sql (id, name) VALUES
(DEFAULT, 'agile_release_train1'),
(DEFAULT,'agile_release_train2');

INSERT INTO data_source_sql (id, name) VALUES
(DEFAULT, 'SBIM');

INSERT INTO subsystem_sql (id, name) VALUES
(DEFAULT, 'Excel');

INSERT INTO connection_type_sql (id, name) VALUES
(DEFAULT, 'Import'),
(DEFAULT,'Live Connection');