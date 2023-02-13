delete from datacompliance_audit_nsql;
delete from datacompliance_nsql;
insert into datacompliance_nsql(id,data) values
('1','{"entityId":"01","entityName":"entityName1","entityCountry":"entityCountry1","localComplianceOfficer":["demouser1@web.com"],"localComplianceResponsible":["demouser2@web.com"],"localComplianceSpecialist":["demouser4@web.com"]}'),
('2','{"entityId":"02","entityName":"entityName2","entityCountry":"entityCountry2","localComplianceOfficer":["demouser5@web.com"],"localComplianceResponsible":["demouser6@web.com"],"localComplianceSpecialist":["demouser8@web.com"]}');
