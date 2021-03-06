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

package com.daimler.data.service.tag;

import org.springframework.http.ResponseEntity;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.db.entities.TagSql;
import com.daimler.data.dto.tag.TagCollection;
import com.daimler.data.dto.tag.TagRequestVO;
import com.daimler.data.dto.tag.TagResponseVO;
import com.daimler.data.dto.tag.TagUpdateRequestVO;
import com.daimler.data.dto.tag.TagVO;
import com.daimler.data.service.common.CommonService;

public interface TagService extends CommonService<TagVO, TagSql, Long> {

	ResponseEntity<TagCollection> getAllTags();

	ResponseEntity<TagResponseVO> createTag(TagRequestVO requestVO);

	ResponseEntity<TagResponseVO> updateTag(TagUpdateRequestVO requestVO);

	ResponseEntity<GenericMessage> deleteTag(long id);

	TagVO findTagByName(String tagName);

}
