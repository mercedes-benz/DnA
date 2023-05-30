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

package com.daimler.data.service.kpiName;

import org.springframework.http.ResponseEntity;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.KpiNameSql;
import com.daimler.data.dto.KpiName.KpiNameRequestVO;
import com.daimler.data.dto.KpiName.KpiNameResponseVO;
import com.daimler.data.dto.KpiName.KpiNameUpdateRequestVO;
import com.daimler.data.dto.KpiName.KpiNameVO;
import com.daimler.data.dto.KpiName.KpiNameVOCollection;
import com.daimler.data.service.common.CommonService;

public interface KpiNameService extends CommonService<KpiNameVO, KpiNameSql, Long> {

	ResponseEntity<KpiNameVOCollection> getAllKpiNames(String sortOrder);

	ResponseEntity<KpiNameResponseVO> createKpiName(KpiNameRequestVO requestVO);

	ResponseEntity<KpiNameResponseVO> updateKpiName(KpiNameUpdateRequestVO requestVO);

	ResponseEntity<GenericMessage> deleteKpiName(Long id);

	KpiNameVO findKpiNameByName(String kpiName);

}
