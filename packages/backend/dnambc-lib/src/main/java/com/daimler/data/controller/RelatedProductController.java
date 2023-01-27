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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.relatedProduct.RelatedProductsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.relatedProduct.RelatedProductRequestVO;
import com.daimler.data.dto.relatedProduct.RelatedProductResponse;
import com.daimler.data.dto.relatedProduct.RelatedProductVO;
import com.daimler.data.dto.relatedProduct.RelatedProductVOCollection;
import com.daimler.data.dto.solution.CreatedByVO;
import com.daimler.data.dto.userinfo.UserInfoVO;
import com.daimler.data.dto.userinfo.UserRoleVO;
import com.daimler.data.service.relatedproduct.RelatedProductService;
import com.daimler.data.service.userinfo.UserInfoService;
import com.daimler.data.util.ConstantsUtility;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "RelatedProducts API", tags = { "relatedProducts" })
@RequestMapping("/api")
@Slf4j
public class RelatedProductController implements RelatedProductsApi {

	@Autowired
	private RelatedProductService relatedProductService;

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserInfoService userInfoService;

	@Override
	@ApiOperation(value = "Adds a new relatedproduct.", nickname = "createRelatedProduct", notes = "Adds a new non existing relatedProduct which is used in providing solution.", response = RelatedProductResponse.class, tags = {
			"relatedProducts", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure ", response = RelatedProductResponse.class),
			@ApiResponse(code = 400, message = "Bad Request", response = com.daimler.data.controller.exceptions.GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/relatedproducts", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<RelatedProductResponse> createRelatedProduct(
			@ApiParam(value = "Request Body that contains data required for creating a new relatedProduct", required = true) @Valid @RequestBody RelatedProductRequestVO relatedProductRequestVO) {
		RelatedProductVO requestRelatedProductVO = relatedProductRequestVO.getData();
		RelatedProductResponse relatedProductResponse = new RelatedProductResponse();
		relatedProductResponse.setData(requestRelatedProductVO);
		try {
			RelatedProductVO existingRelatedProductVO = relatedProductService.getByUniqueliteral("name",
					requestRelatedProductVO.getName());
			if (existingRelatedProductVO != null && existingRelatedProductVO.getName() != null) {
				relatedProductResponse.setData(existingRelatedProductVO);
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Related Product `" + existingRelatedProductVO.getName() + "` already exists.");
				messages.add(message);
				relatedProductResponse.setErrors(messages);
				log.info("Related product {} already exists, cannot create. Returning with conflict.",
						existingRelatedProductVO.getName());
				return new ResponseEntity<>(relatedProductResponse, HttpStatus.CONFLICT);
			}
			requestRelatedProductVO.setId(null);
			RelatedProductVO relatedProductVo = relatedProductService.create(requestRelatedProductVO);
			if (relatedProductVo != null && relatedProductVo.getId() != null) {
				relatedProductResponse.setData(relatedProductVo);
				log.info("Related product {} created successfully", requestRelatedProductVO.getName());
				return new ResponseEntity<>(relatedProductResponse, HttpStatus.CREATED);
			} else
				log.info("Unknown error occured while creating Related-product {} ", requestRelatedProductVO.getName());
			return new ResponseEntity<>(relatedProductResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			log.error("Exception {} , occured while creating Related-product {}", e.getLocalizedMessage(),
					requestRelatedProductVO.getName());
			return new ResponseEntity<>(relatedProductResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@ApiOperation(value = "Deletes the tag identified by given ID.", nickname = "delete", notes = "Deletes the tag identified by given ID", response = com.daimler.data.controller.exceptions.GenericMessage.class, tags = {
			"relatedProducts", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully deleted.", response = com.daimler.data.controller.exceptions.GenericMessage.class),
			@ApiResponse(code = 400, message = "Bad request"),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 404, message = "Invalid id, record not found."),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/relatedProducts/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.DELETE)
	public ResponseEntity<com.daimler.data.controller.exceptions.GenericMessage> delete(
			@ApiParam(value = "Id of the relatedproduct", required = true) @PathVariable("id") String id) {
		try {
			CreatedByVO currentUser = this.userStore.getVO();

			String userId = currentUser != null ? currentUser.getId() : "";

			if (userId != null && !"".equalsIgnoreCase(userId)) {
				UserInfoVO userInfoVO = userInfoService.getById(userId);
				if (userInfoVO != null) {
					List<UserRoleVO> userRoleVOs = userInfoVO.getRoles();
					if (userRoleVOs != null && !userRoleVOs.isEmpty()) {
						boolean isAdmin = userRoleVOs.stream().anyMatch(n -> "Admin".equalsIgnoreCase(n.getName()));
						if (userId == null || !isAdmin) {
							MessageDescription notAuthorizedMsg = new MessageDescription();
							notAuthorizedMsg.setMessage(
									"Not authorized to delete related products. User does not have admin privileges.");
							GenericMessage errorMessage = new GenericMessage();
							errorMessage.addErrors(notAuthorizedMsg);
							log.debug("User not authorized to delete Related-product");
							return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
						}
					}
				}
			}
			// relatedProductService.deleteById(id);
			RelatedProductVO relatedProduct = relatedProductService.getById(id);
			String relatedProductName = relatedProduct != null ? relatedProduct.getName() : "";
			String userName = relatedProductService.currentUserName(currentUser);
			String eventMessage = "RelatedProduct  " + relatedProductName + " has been deleted by Admin " + userName;
			relatedProductService.deleteRelatedProduct(id);
			userInfoService.notifyAllAdminUsers(ConstantsUtility.SOLUTION_MDM, id, eventMessage, userId, null);
			GenericMessage successMsg = new GenericMessage();
			successMsg.setSuccess("success");
			log.info("Related-product {} deleted successfully", id);
			return new ResponseEntity<>(successMsg, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			// log.error(e.getLocalizedMessage());
			MessageDescription invalidMsg = new MessageDescription("No related product with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			log.info("Related-product {} not found to be deleted", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			log.error("Failed to delete related product {} with exception", id, e.getLocalizedMessage());
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Get all realtedProducts.", nickname = "getAll", notes = "Get all related products. This endpoints will be used to Get all valid available relatedproducts.", response = RelatedProductVOCollection.class, tags = {
			"relatedProducts", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = RelatedProductVOCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/relatedproducts", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<RelatedProductVOCollection> getAll(
			@ApiParam(value = "Sort relatedproducts based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder) {
		final List<RelatedProductVO> relatedProducts = relatedProductService.getAll();
		
		RelatedProductVOCollection relatedProductVOCollection = new RelatedProductVOCollection();
		if (relatedProducts != null && relatedProducts.size() > 0) {
			if( sortOrder == null || sortOrder.equalsIgnoreCase("asc")) {	
				relatedProducts.sort(Comparator.comparing(RelatedProductVO :: getName, String.CASE_INSENSITIVE_ORDER));
			}
			if(sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
				relatedProducts.sort(Comparator.comparing(RelatedProductVO :: getName, String.CASE_INSENSITIVE_ORDER).reversed());
			}
			relatedProductVOCollection.addAll(relatedProducts);
			log.debug("Returning all available related-products");
			return new ResponseEntity<>(relatedProductVOCollection, HttpStatus.OK);
		} else {
			log.debug("No related-products found, returning empty");
			return new ResponseEntity<>(relatedProductVOCollection, HttpStatus.NO_CONTENT);
		}

	}

}
