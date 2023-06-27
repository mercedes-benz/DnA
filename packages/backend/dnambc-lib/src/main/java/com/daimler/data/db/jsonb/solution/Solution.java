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

package com.daimler.data.db.jsonb.solution;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Solution {

	private String productName;
	private String description;
	private String businessNeed;
	private List<String> businessGoals;
	private String expectedBenefits;
	private String reasonForHoldOrClose;

	private List<SolutionLocation> locations;
	private SolutionDivision division;
	private SolutionProjectStatus projectStatus;
	private boolean publish;

	private List<String> tags;

	private List<String> openSegments;

	private List<SolutionTeamMember> teamMembers;

	private List<SolutionMilestone> milestones;

	private SolutionRollOut rollout;

	private List<SolutionDatasource> dataSources;

	private List<SolutionLanguage> languages;

	private List<SolutionAlgorithm> algorithms;

	private List<SolutionVisualization> visualizations;
	
	private List<SolutionCustomerJourneyPhase> customerJourneyPhases;
	
	private List<SolutionMarketingCommunicationChannel> marketingCommunicationChannels;
	
	private SolutionPersonalization personalization;
	
	private List<String> personas;
	
	private String department;

	private String gitUrl;
	private String resultUrl;
	private SolutionResult result;

	private SolutionDigitalValue digitalValueDetails;
	private SolutionDataCompliance dataComplianceDetails;

	private SolutionDataVolume totalDataVolume;

	private boolean solutionOnCloud;
	private boolean usesExistingInternalPlatforms;
	private String dnaNotebookId;
	private String dataikuProjectKey;
	private String dataikuProjectInstance;
	private String dnaSubscriptionAppId;
	private List<SolutionPlatform> platforms;

	private Date createdDate;
	private Date lastModifiedDate;
	private Date closeDate;
	private CreatedBy createdBy;

	private CurrentPhase currentPhase;

	private List<FileDetails> attachments;

	private List<String> relatedProducts;

	private LogoDetails logoDetails;
	
	private List<SkillSummary> skills;
	private List<MarketingRoleSummary> marketingRoles;
	private String dataStrategyDomain;
	private String additionalResource;
	private BigDecimal requestedFTECount;
	
}
