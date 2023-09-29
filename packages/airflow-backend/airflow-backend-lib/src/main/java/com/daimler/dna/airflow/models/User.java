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

package com.daimler.dna.airflow.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.JoinColumn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ab_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "user_seq")
	@SequenceGenerator(name = "user_seq", sequenceName = "ab_user_id_seq", allocationSize = 1)
	private Integer id;
	private String firstName;
	private String lastName;
	private String username;
	private String password = "pbkdf2:sha256:150000$5q6JkIVM$a5a47be7a11692c4950a4b5ccf4db02281c69a37158f5160f17a0b63fa816719";
	private Boolean active;
	private String email;
	private Date lastLogin;
	private Integer loginCount;
	private Integer failLoginCount;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createdOn = new Date();

	private Date changedOn;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL,  fetch = FetchType.LAZY)
	private List<UserRoleMapping> roleMaping;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL,  fetch = FetchType.LAZY)
	private List<DnaProjectUserAndDagMapping> dnaProjectUserAndDagMapping;	
	
}
