CREATE TABLE IF NOT EXISTS kpi_classification_sql
(
    id               BIGINT             DEFAULT nextval('lov_sequence') PRIMARY KEY,
    name             VARCHAR(255)       
);

INSERT INTO kpi_classification_sql (id, name) VALUES
(DEFAULT,'Overhead & Invest > Overhead Controlling'),
(DEFAULT,'Overhead & Invest > Overhead Planning'),
(DEFAULT,'Overhead & Invest > Plan Cost Breakdown'),
(DEFAULT,'Overhead & Invest > Funding Planning & Reporting'),
(DEFAULT,'Overhead & Invest > Depreciation calculations'),
(DEFAULT,'Overhead & Invest > Journalization of funding/invest'),
(DEFAULT,'Overhead & Invest > Purchasing E2E Process'),
(DEFAULT,'Overhead & Invest > Shift Approval'),
(DEFAULT,'Product Cost Controlling > Manufactoring Cost Rates Calculation'),
(DEFAULT,'Product Cost Controlling > Product Costing Hub'),
(DEFAULT,'Product Cost Controlling > Product Change Management'),
(DEFAULT,'Product Cost Controlling > Production quantities'),
(DEFAULT,'Product Cost Controlling > Production Material Controlling'),
(DEFAULT,'Product Cost Controlling > Price calculation for PBP (BBAC)'),
(DEFAULT,'Inventory & Working Capital > Inventory Controlling'),
(DEFAULT,'Inventory & Working Capital > Working Capital'),
(DEFAULT,'Sales & Leasing > Sales & Revenue Controlling/Planning'),
(DEFAULT,'Sales & Leasing > Residual value setting & Portfolio valuation'),
(DEFAULT,'Sales & Leasing > Digital Services'),
(DEFAULT,'Sales & Leasing > Margin Evaluator'),
(DEFAULT,'Profitability Analysis > Mercedes-Benz Business Planning'),
(DEFAULT,'Profitability Analysis > Project Tracking'),
(DEFAULT,'Profitability Analysis > OnePL'),
(DEFAULT,'Profitability Analysis > Market Profitability Analysis - OneCM'),
(DEFAULT,'Profitability Analysis > FX Exposure'),
(DEFAULT,'Profitability Analysis > FX Effect Calculation');

ALTER TABLE kpi_name_sql
ADD classification varchar(255);
