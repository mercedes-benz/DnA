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

package com.daimler.data.dataiku.client;

import java.util.Optional;

import com.daimler.data.dto.DataikuConnectionRequestDTO;
import com.daimler.data.dto.DataikuGenericResponseDTO;
import com.daimler.data.dto.DataikuPermission;

public interface DataikuClient {

	/**
	 * <p>
	 * To get dataiku project permission
	 * </p>
	 * 
	 * @param projectKey
	 * @param live
	 * @return DataikuPermission
	 */
	public Optional<DataikuPermission> getDataikuProjectPermission(String projectKey, Boolean live);

	/**
	 * To create dataiku connection
	 * 
	 * @param requestDTO {@code DataikuConnectionRequestDTO}
	 * @param live
	 * @return response {@code DataikuGenericResponseDTO}
	 */
	public DataikuGenericResponseDTO createDataikuConnection(DataikuConnectionRequestDTO requestDTO, Boolean live);

	/**
	 * To delete dataiku connection
	 * 
	 * @param connectionName
	 * @param live
	 * @return response {@code DataikuGenericResponseDTO}
	 */
	public DataikuGenericResponseDTO deleteDataikuConnection(String connectionName, Boolean live);

}
