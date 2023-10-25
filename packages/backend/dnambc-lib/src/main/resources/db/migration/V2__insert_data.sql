insert into additionalresources_nsql(id,data)
values('1','{"name": "additionalresources1"}'),
      ('2','{"name": "additionalresources2"}');
	  
insert into algorithm_nsql(id,data)
values('1','{"name": "algorithm1"}'),
('2','{"name": "algorithm2"}');
	  
insert into benefitrelevance_nsql(id,data)
values('1','{"name": "benefitrelevance1"}'),
      ('2','{"name": "benefitrelevance2"}');
	  
insert into businessgoal_nsql(id,data)
values('1','{"name": "businessgoal1"}'),
      ('2','{"name": "businessgoal2"}');
	
insert into category_nsql(id,data)
values('1','{"name": "category1"}'),
      ('2','{"name": "category2"}');
	  
insert into datasource_nsql (id, data)
values ('1','{"name": "datasource1"}'),
('2','{"name": "datasource2"}');

insert into datastrategydomain_nsql(id,data)
values('1','{"name": "datastrategydomain1"}'),
      ('2','{"name": "datastrategydomain2"}');
	 
insert into datavolume_nsql(id,data)
values('1','{"name": "datavolume1"}'),
      ('2','{"name": "datavolume2"}');
	  
insert into division_nsql (id, data)
values('1','{"name": "division1", "subdivisions": [{"id": "1", "name": "subdivision1"}, {"id": "2", "name": "subdivision2"}]}'),
	  ('2','{"name": "division2", "subdivisions": [{"id": "1", "name": "subdivision1"}, {"id": "2", "name": "subdivision2"}]}');

insert into language_nsql (id, data) 
values('1','{"name": "language1"}'),
('2','{"name": "language2"}');

insert into location_nsql (id, data) 
values('1','{"name": "location1", "is_row": true}'),
('2','{"name": "location2", "is_row": true}');

insert into maturitylevel_nsql (id, data) 
values('1','{"name": "maturitylevel1"}'),
('2','{"name": "maturitylevel2"}');

insert into phase_nsql (id, data) 
values('1','{"name": "phase1"}'),
('2','{"name": "phase2"}');


insert into platform_nsql (id, data) 
values('1','{"name": "platform1"}'),
('2','{"name": "platform2"}');

insert into projectstatus_nsql (id, data) 
values('1','{"name": "projectstatus1"}'),
('2','{"name": "projectstatus2"}');

insert into relatedproduct_nsql (id, data) 
values('1','{"name": "relatedproduct1"}'),
('2','{"name": "relatedproduct2"}');

insert into result_nsql (id, data) 
values('1','{"name": "result1"}'),
('2','{"name": "result2"}');

insert into skill_nsql (id, data) 
values('1','{"name": "skill1"}'),
('2','{"name": "skill2"}');

insert into strategicrelevance_nsql (id, data) 
values('1','{"name": "strategicrelevance1"}'),
('2','{"name": "strategicrelevance2"}');

insert into topic_nsql (id, data) 
values('1','{"name": "topic1"}'),
('2','{"name": "topic2"}');

insert into userrole_nsql (id, data) 
values('1','{"name": "userrole1"}'),
('2','{"name": "userrole2"}');

insert into visualization_nsql (id, data)
values('1','{"name": "visualization1"}'),
('2','{"name": "visualization2"}');

insert into userinfo_nsql (id, data, is_logged_in) 
values('DEMOUSER','{"email": "demouser@web.de", "roles": [{"id": "3", "name": "Admin"}], "lastName": "User", "firstName": "Demo", "department": "TE/ST", "mobileNumber": "", "favoriteUsecases":[]}', 'Y');