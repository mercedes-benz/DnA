/* LICENSE START
 * 
 * MIT License
 *
 * Copyright (c) 2019 Daimler TSS GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * LICENSE END
 */

package com.daimler.dna.airflow.repository;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.daimler.dna.airflow.models.DnaProject;

@Repository
public class DnaProjectRepositoryImpl extends CommonDataRepositoryImpl<DnaProject, Integer>
		implements DnaProjectRepository {

	@Override
	public List<Object[]> findAllProjectsByUserId(String username) {
		String query = "select dna_project.project_id,dna_project.created_by,dna_project_user_dag.dag_id, ab_permission.name, dna_project.project_name, dna_project.project_description  from ab_user inner join dna_project_user_dag\r\n"
				+ "on dna_project_user_dag.user_id = ab_user.id\r\n"
				+ "inner join dna_project_user on dna_project_user_dag.id = dna_project_user.user_dag_id\r\n"
				+ "inner join dna_project on dna_project.id = dna_project_user.dna_project_id\r\n"
				+ "inner join ab_view_menu ON dna_project_user_dag.dag_id = SUBSTRING(ab_view_menu.name, POSITION(':' IN ab_view_menu.name) + 1)\r\n"
				+ "inner join ab_permission_view on ab_permission_view.view_menu_id = ab_view_menu.id\r\n"
				+ "inner join ab_permission on ab_permission.id = ab_permission_view.permission_id\r\n"
				+ "where ab_permission.name in ('can_read', 'can_edit') and username = ?";

		Query qry = this.em.createNativeQuery(query);
		qry.setParameter(1, username);
		return qry.getResultList();
	}

	@Override
	public List<Object[]> findDagPermissionAndViewMenu(String dagName) {
		String query = "select ab_permission_view.id from ab_permission inner join ab_permission_view\r\n"
				+ "on ab_permission.id = ab_permission_view.permission_id\r\n"
				+ "inner join ab_view_menu on ab_permission_view.view_menu_id = ab_view_menu.id\r\n"
				+ "where ab_permission.name in ('can_read', 'can_edit')\r\n" + "and (ab_view_menu.name = ? OR ab_view_menu.name = CONCAT('DAG:', ?))";

		Query qry = this.em.createNativeQuery(query);
		qry.setParameter(1, dagName);
		qry.setParameter(2, dagName);
		return qry.getResultList();
	}

	@Override
	public Integer deleteUserAndProjectMapping(int projectId) {
		String query = "delete from dna_project_user where dna_project_id = ?";
		Query qry = this.em.createNativeQuery(query);
		qry.setParameter(1, projectId);
		return qry.executeUpdate();
	}

	@Override
	public Integer deleteUserAndDagMapping(int id) {
		String query = "delete from dna_project_user_dag where id = ?";
		Query qry = this.em.createNativeQuery(query);
		qry.setParameter(1, id);
		return qry.executeUpdate();
	}

	@Override
	public Integer deleteRoleAndPermission(int id) {
		String query = "delete from ab_permission_view_role where id = ?";
		Query qry = this.em.createNativeQuery(query);
		qry.setParameter(1, id);
		return qry.executeUpdate();
	}

	@Override
	public Integer updateProject(DnaProject updatedProject) {
		String query = "update dna_project set project_name=:projectName, project_description=:projectDescription where project_id=:projectId";
		Query qry = this.em.createNativeQuery(query);
		qry.setParameter("projectName", updatedProject.getProjectName());
		qry.setParameter("projectDescription", updatedProject.getProjectDescription());
		qry.setParameter("projectId", updatedProject.getProjectId());
		return qry.executeUpdate();
	}

	@Override
	public List<Object[]> dagPermissionList(String dagName, String user) {
		String query = "select ab_permission.name, ab_role.name as role  from ab_user inner join ab_user_role on ab_user.id = ab_user_role.user_id\r\n"
				+ "inner join ab_role on ab_user_role.role_id =  ab_role.id\r\n"
				+ "inner join ab_permission_view_role on ab_role.id = ab_permission_view_role.role_id\r\n"
				+ "inner join ab_permission_view on ab_permission_view_role.permission_view_id = ab_permission_view.id\r\n"
				+ "inner join ab_permission on ab_permission_view.permission_id = ab_permission.id\r\n"
				+ "inner join ab_view_menu on ab_permission_view.view_menu_id = ab_view_menu.id\r\n"
				+ "where ab_permission.name in ('can_read', 'can_edit') and\r\n"
				+ "ab_user.username = ?\r\n"
				+ "and (ab_view_menu.name = ? OR ab_view_menu.name = CONCAT('DAG:', ?))";

		Query qry = this.em.createNativeQuery(query);
		qry.setParameter(1, user);
		qry.setParameter(2, dagName);
		qry.setParameter(3, dagName);
		return qry.getResultList();
	}

	public Integer deletedag(String dagName) {
		Integer deletedRow = null;
		String query = "DELETE FROM dna_project_user "
				+ "WHERE user_dag_id IN (SELECT id FROM dna_project_user_dag WHERE dag_id = ?)";
		Query qry = this.em.createNativeQuery(query);
		qry.setParameter(1, dagName);
		deletedRow = qry.executeUpdate();

		if (deletedRow != null && deletedRow > 0) {
			// List of tables from which DAG to be deleted
			List<String> listOfTableTobeDeleted = Arrays.asList("dna_project_user_dag", "xcom", "dag_tag",
					"task_instance", "sla_miss", "log", "job", "dag_run", "dag");
			for (String table : listOfTableTobeDeleted) {
				query = "delete from " + table + " where dag_id = ?";
				qry = this.em.createNativeQuery(query);
				qry.setParameter(1, dagName);
				deletedRow = qry.executeUpdate();
			}
		}
		return deletedRow;
	}

}
