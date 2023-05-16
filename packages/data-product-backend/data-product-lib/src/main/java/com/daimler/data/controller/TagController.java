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

package com.daimler.data.controller;

import java.util.Comparator;
import java.util.List;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.tag.TagsApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.dto.tag.TagCollection;
import com.daimler.data.dto.tag.TagRequestVO;
import com.daimler.data.dto.tag.TagVO;
import com.daimler.data.service.tag.TagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Tag API", tags = { "tags" })
@RequestMapping("/api")
@Slf4j
public class TagController implements TagsApi {

	@Autowired
	private TagService tagService;

	@Override
	 @ApiOperation(value = "Adds a new tag.", nickname = "create", notes = "Adds a new non existing tag which is used in providing solution.", response = TagVO.class, tags={ "tags", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = TagVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/tags",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<TagVO> create(
			@ApiParam(value = "Request Body that contains data required for creating a new tag" ,required=true )
			@Valid TagRequestVO tagRequestVO) {
		// TODO Auto-generated method stub
		TagVO requestTagVO = tagRequestVO.getData();
		try {
			TagVO existingTagVO = tagService.getByUniqueliteral("name", requestTagVO.getName());
			if (existingTagVO != null && existingTagVO.getName() != null) {
				log.info("Tag with name {} already exists, returning with conflict", requestTagVO.getName());
				return new ResponseEntity<>(existingTagVO, HttpStatus.CONFLICT);
			}
			requestTagVO.setId(null);
			TagVO tagVo = tagService.create(requestTagVO);
			if (tagVo != null && tagVo.getId() != null) {
				log.info("Tag with name {} created successfully", requestTagVO.getName());
				return new ResponseEntity<>(tagVo, HttpStatus.CREATED);
			} else
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			log.error("Failed to create tag with name {} with exception {}", requestTagVO.getName(),
					e.getLocalizedMessage());
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Deletes the tag identified by given ID.", nickname = "delete", notes = "Deletes the tag identified by given ID", response = GenericMessage.class, tags={ "tags", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully deleted.", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 404, message = "Invalid id, record not found."),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/tags/{id}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
	public ResponseEntity<GenericMessage> delete(@ApiParam(value = "Id of the tag",required=true) @PathVariable("id") String id) {		
		// TODO Auto-generated method stub
		return tagService.deleteTag(id);
	}

	@Override
	@ApiOperation(value = "Get all available tags.", nickname = "getAll", notes = "Get all tags. This endpoints will be used to Get all valid available tags maintenance records.", response = TagCollection.class, tags={ "tags", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure", response = TagCollection.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/tags",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<TagCollection> getAll(@ApiParam(value = "Sort tags based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		// TODO Auto-generated method stub

		final List<TagVO> tags = tagService.getAll();		
		TagCollection tagCollection = new TagCollection();
		if (tags != null && tags.size() > 0) {
			if( sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {
				tags.sort(Comparator.comparing(TagVO :: getName, String.CASE_INSENSITIVE_ORDER));
			}
			if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
				tags.sort(Comparator.comparing(TagVO :: getName, String.CASE_INSENSITIVE_ORDER).reversed());
			}
			tagCollection.addAll(tags);
			log.info("Returning available tags");
			return new ResponseEntity<>(tagCollection, HttpStatus.OK);
		} else {
			log.info("No tags available, returning empty");
			return new ResponseEntity<>(tagCollection, HttpStatus.NO_CONTENT);
		}
	
	}

	@Override
	@ApiOperation(value = "Updates a existing tag.", nickname = "update", notes = "Updates a new non existing tag which is used in dataproduct.", response = TagVO.class, tags={ "tags", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of succes or failure ", response = TagVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/tags",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
	public ResponseEntity<TagVO> update(
			@ApiParam(value = "Request Body that contains data required for updating a existing tag" ,required=true )
			@Valid TagRequestVO tagRequestVO) {
		// TODO Auto-generated method stub
		return tagService.updateTag(tagRequestVO);
	}
	
	}
