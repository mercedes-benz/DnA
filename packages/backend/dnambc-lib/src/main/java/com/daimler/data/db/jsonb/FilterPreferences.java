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

package com.daimler.data.db.jsonb;

import com.daimler.data.db.jsonb.solution.SolutionDivision;
import com.daimler.data.db.jsonb.solution.SolutionLocation;
import com.daimler.data.db.jsonb.solution.SolutionPhase;
import com.daimler.data.db.jsonb.solution.SolutionProjectStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterPreferences implements Serializable {

	private static final long serialVersionUID = 4051188801188093512L;

	private String useCaseType;
	private List<SolutionPhase> phases;
	private List<UserWidgetPreferenceDivision> divisions;
	private List<SolutionLocation> locations;
	private SolutionProjectStatus solutionStatus;
	private List<Tag> tags;
	private String dataValueRange;

//	public FilterPreferences() {
//		super();
//	}
//
//	public FilterPreferences(List<SolutionPhase> phases, List<SolutionDivision> divisions,
//			List<SolutionLocation> locations, SolutionProjectStatus solutionStatus) {
//		super();
//		this.phases = phases;
//		this.divisions = divisions;
//		this.locations = locations;
//		this.solutionStatus = solutionStatus;
//	}
//
//	public List<SolutionPhase> getPhases() {
//		return phases;
//	}
//
//	public void setPhases(List<SolutionPhase> phases) {
//		this.phases = phases;
//	}
//
//	public List<SolutionDivision> getDivisions() {
//		return divisions;
//	}
//
//	public void setDivisions(List<SolutionDivision> divisions) {
//		this.divisions = divisions;
//	}
//
//	public List<SolutionLocation> getLocations() {
//		return locations;
//	}
//
//	public void setLocations(List<SolutionLocation> locations) {
//		this.locations = locations;
//	}
//
//	public SolutionProjectStatus getSolutionStatus() {
//		return solutionStatus;
//	}
//
//	public void setSolutionStatus(SolutionProjectStatus solutionStatus) {
//		this.solutionStatus = solutionStatus;
//	}
//
}
