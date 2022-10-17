INSERT INTO data_classification_sql (id, name) VALUES
(DEFAULT, 'Public'),
(DEFAULT,'Confidential'),
(DEFAULT, 'Internal'),
(DEFAULT,'Secret');

INSERT INTO status_sql (id, name) VALUES
(DEFAULT, 'Active'),
(DEFAULT,'On hold'),
(DEFAULT,'Closed');

INSERT INTO integrated_portal_sql (id, name) VALUES
(DEFAULT, 'FC Cockpit'),
(DEFAULT,'myFM'),
(DEFAULT,'Other');

INSERT INTO frontend_technology_sql (id, name) VALUES
(DEFAULT, 'Power BI'),
(DEFAULT,'Tableau'),
(DEFAULT,'Lumira Design'),
(DEFAULT,'Customized Application'),
(DEFAULT,'FIORI'),
(DEFAULT,'SAC'),
(DEFAULT,'AFO'),
(DEFAULT,'Other');

INSERT INTO level_sql (id, name) VALUES
(DEFAULT, 'Management (E3/E4)'),
(DEFAULT,'Staff (SB)'),
(DEFAULT, 'Top-Management (E1/E2)');

INSERT INTO customer_department_sql (id, name) VALUES
(DEFAULT,'GL'),
(DEFAULT,'BF'),
(DEFAULT,'FMC'),
(DEFAULT,'FMS'),
(DEFAULT,'FMB'),
(DEFAULT,'FMV'),
(DEFAULT,'FC'),
(DEFAULT,'FAO'),
(DEFAULT,'FAG'),
(DEFAULT,'FAP'),
(DEFAULT,'FAB'),
(DEFAULT,'FAM'),
(DEFAULT,'FA'),
(DEFAULT,'FF'),
(DEFAULT,'FI'),
(DEFAULT,'IPS'),
(DEFAULT,'ITG');

INSERT INTO kpi_name_sql (id, name) VALUES
(DEFAULT, 'Contribution Margin per Unit'),
(DEFAULT, 'CAV'),
(DEFAULT, 'ROI'),
(DEFAULT, 'Cost per Acquisition'),
(DEFAULT, 'EBIT'),
(DEFAULT, 'TestKPI');

INSERT INTO reporting_cause_sql (id, name) VALUES
(DEFAULT, 'IST'),
(DEFAULT,'Plan'),
(DEFAULT,'Forecast');

INSERT INTO connection_type_sql (id, name) VALUES
(DEFAULT, 'Import'),
(DEFAULT,'Live Connection');