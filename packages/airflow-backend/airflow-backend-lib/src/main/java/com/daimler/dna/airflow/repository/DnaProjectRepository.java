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

import java.util.List;

import com.daimler.dna.airflow.models.DnaProject;

public interface DnaProjectRepository extends CommonDataRepository<DnaProject, Integer> {

	List<Object[]> findAllProjectsByUserId(String username);

	List<Object[]> findAllCreationStatusProjectsByUserId(String username, String status);

	List<Object[]> findAllCreationStatusProjects(String status);

	List<Object[]> findDagPermissionAndViewMenu(String dagName);

	Integer deleteUserAndProjectMapping(int projectId);

	Integer deleteUserAndDagMapping(int id);

	Integer deleteRoleAndPermission(int id);

	Integer updateProject(DnaProject updatedProject);

	List<Object[]> dagPermissionList(String dagName, String user);

	public Integer deletedag(String dagName);

	Integer updateProjectStatus(String projectId, String projectStatus);

	List<String> findPermissionNameforGivenUserDag(String dagName, String userName);
}
