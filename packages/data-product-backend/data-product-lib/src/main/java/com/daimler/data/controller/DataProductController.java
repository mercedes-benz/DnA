package com.daimler.data.controller;

import com.daimler.data.api.dataproduct.DataproductsApi;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.DataProductAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.dataproduct.*;
import com.daimler.data.dto.datatransfer.*;
import com.daimler.data.service.dataproduct.DataProductService;
import com.daimler.data.service.datatransfer.DataTransferService;
import com.daimler.data.util.ConstantsUtility;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@Api(value = "Dataproduct API", tags = { "dataproducts" })
@RequestMapping("/api")
@Slf4j
@SuppressWarnings(value = "unused")
public class DataProductController implements DataproductsApi{

	@Autowired
	private DataProductService service;

	@Autowired
	private DataTransferService dataTransferService;

	@Autowired
	private UserStore userStore;

	@Autowired
	private DataProductAssembler assembler;

	@Value("${dataproduct.refreshdb}")
	private Boolean dataproductRefreshDb;

	@Value("${dataproduct.refreshdb}")
	private Boolean datatransferRefreshDb;

	@Value("${xapikey.val}")
	private String xApiKeyValue;

	@Autowired
	HttpServletRequest httpRequest;

	@ApiOperation(value = "Add a new dataproduct", nickname = "create", notes = "Adds a new non existing dataproduct", response = DataProductResponseVO.class, tags={ "dataproducts", })
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Returns message of success", response = DataProductResponseVO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/dataproducts",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.POST)
    public ResponseEntity<DataProductResponseVO> create(@ApiParam(value = "Request Body that contains data required for creating a new dataproduct" ,required=true )  @Valid @RequestBody DataProductRequestVO dataProductRequestVO){
		DataProductResponseVO responseVO = new DataProductResponseVO();
		DataProductVO dataProductVO = new DataProductVO();
		DataProductVO requestVO = dataProductRequestVO.getData();
		try {
			CreatedByVO currentUser = this.userStore.getVO();
			String userId = currentUser != null ? currentUser.getId() : "";
			String uniqueProductName = requestVO.getDataProductName();
			List<DataProductVO> dataProductVOs = service.getExistingDataProduct(uniqueProductName, ConstantsUtility.OPEN);
			if (!ObjectUtils.isEmpty(dataProductVOs) && (dataProductVOs.size()>0)) {
				responseVO.setData(dataProductVOs.get(0));
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Data Product already exisits.");
				messages.add(message);
				responseVO.setErrors(messages);
				log.info("DataProductVO {} already exists, returning as CONFLICT", uniqueProductName);
				return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
			}
			if(requestVO.isIsPublish()) {
				if(Objects.isNull(requestVO.getClassificationConfidentiality()) || 
				   Objects.isNull(requestVO.getContactInformation()) ||
				   Objects.isNull(requestVO.getPersonalRelatedData()) ||
				   Objects.isNull(requestVO.getTransnationalDataTransfer())||
				   Objects.isNull(requestVO.getDeletionRequirement()) || Objects.isNull(requestVO.getDeletionRequirement().getInsiderInformation())) {
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("DataProduct cannot be created as user is not providing the values for one of the tabs.");
					messages.add(message);
					responseVO.setErrors(messages);
					log.info("DataProduct {} cannot be created as user is not providing the values for one of the tabs", uniqueProductName);
					return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
				}
			}
			requestVO.setCreatedBy(this.userStore.getVO());
			requestVO.setCreatedDate(new Date());
			requestVO.setIsPublish(false);
			requestVO.setRecordStatus(ConstantsUtility.OPEN);
			requestVO.setDataProductId("DPF-" + service.getNextSeqId());
			requestVO.setId(null);
			DataProductVO vo = service.create(requestVO);
			if (vo != null && vo.getId() != null) {
				responseVO.setData(vo);
				log.info("DataProduct {} created successfully", uniqueProductName);
				return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
			} else {
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				responseVO.setData(requestVO);
				responseVO.setErrors(messages);
				log.error("DataProduct {} , failed to create", uniqueProductName);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			log.error("Exception occurred:{} while creating DataProduct {} ", e.getMessage(),
					requestVO.getDataProductName());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			responseVO.setData(requestVO);
			responseVO.setErrors(messages);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Refresh DB records for data-product and data-transfer.", nickname = "dbRecordsRefresh", notes = "Refresh DB records for data-product and data-transfer.", response = DataProductCollection.class, tags={ "dataproducts", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = DataProductCollection.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dataproducts/dbrecords/refresh",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.GET)
	public ResponseEntity<RefreshVo> dbRecordsRefresh() {
		RefreshVo refreshVoResponse = new RefreshVo();
		GenericMessage dataproductGenericMessage = new GenericMessage();
		GenericMessage dataTransferGenericMessage = new GenericMessage();
		HttpHeaders headers = new HttpHeaders();
		String xApiKey = httpRequest.getHeader("x-api-key");

		if (xApiKey != null && xApiKey.equals(xApiKeyValue)) {
			if (dataproductRefreshDb) {
				dataproductGenericMessage = service.updateDataProductData();
				refreshVoResponse.setDataproductGenericMessage(dataproductGenericMessage);
			} else {
				dataproductGenericMessage.setSuccess(ConstantsUtility.FAILURE);
				MessageDescription messageDescription = new MessageDescription();
				messageDescription.setMessage("dataproduct refresh db setting is not enabled");
				dataproductGenericMessage.addErrors(messageDescription);
				refreshVoResponse.setDataproductGenericMessage(dataproductGenericMessage);
			}
			if (datatransferRefreshDb) {
				dataTransferGenericMessage = dataTransferService.updateDataTransferData();
				refreshVoResponse.setDatatransferGenericMessage(dataTransferGenericMessage);
			} else {
				dataTransferGenericMessage.setSuccess(ConstantsUtility.FAILURE);
				MessageDescription messageDescription = new MessageDescription();
				messageDescription.setMessage("datatransfer refresh db setting is not enabled");
				dataTransferGenericMessage.addErrors(messageDescription);
				refreshVoResponse.setDatatransferGenericMessage(dataTransferGenericMessage);
			}
		} else {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(refreshVoResponse, HttpStatus.OK);
	}


	@ApiOperation(value = "Delete dataproduct for a given Id.", nickname = "delete", notes = "Delete dataproduct for a given identifier.", response = GenericMessage.class, tags={ "dataproducts", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/dataproducts/{id}",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.DELETE)
    public ResponseEntity<GenericMessage> delete(@ApiParam(value = "DataProduct ID to be deleted",required=true) @PathVariable("id") String id){
    	try {
			DataProductVO existingVO = service.getById(id);
			DataProductVO dataProductVO = null;
			if (existingVO != null && existingVO.getId() != null && ConstantsUtility.OPEN.equalsIgnoreCase(existingVO.getRecordStatus())) {
				CreatedByVO createdBy = existingVO.getCreatedBy();
				CreatedByVO requestUser = this.userStore.getVO();
				DataProductTeamMemberVO nameUser = new DataProductTeamMemberVO();
				DataProductTeamMemberVO informationOwner = new DataProductTeamMemberVO();
				if (existingVO.getContactInformation() != null && existingVO.getContactInformation().getName() != null) {
					nameUser = existingVO.getContactInformation().getName();
				}
				if (existingVO.getContactInformation() != null && existingVO.getContactInformation().getInformationOwner() != null) {
					informationOwner =  existingVO.getContactInformation().getInformationOwner();
				}
				if (requestUser.getId().equalsIgnoreCase(createdBy.getId())
						|| (informationOwner.getShortId() != null && requestUser.getId().equalsIgnoreCase(informationOwner.getShortId()))
						|| (nameUser.getShortId() != null && requestUser.getId().equalsIgnoreCase(nameUser.getShortId()))
				) {
					existingVO.lastModifiedDate(new Date());
					existingVO.setModifiedBy(requestUser);
					existingVO.setRecordStatus(ConstantsUtility.DELETED);
					dataProductVO = service.create(existingVO);
					if (dataProductVO != null && dataProductVO.getId() != null) {
						GenericMessage successMsg = new GenericMessage();
						successMsg.setSuccess("success");
						log.info("DataProduct with id {} deleted successfully", id);
						return new ResponseEntity<>(successMsg, HttpStatus.OK);
					} else {
						MessageDescription exceptionMsg = new MessageDescription(
								"Failed to delete dataProduct due to internal error");
						GenericMessage errorMessage = new GenericMessage();
						errorMessage.addErrors(exceptionMsg);
						log.info("DataProduct with id {} cannot be deleted. Failed with unknown internal error",
								id);
						return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
					}

				} else {
					MessageDescription notAuthorizedMsg = new MessageDescription();
					notAuthorizedMsg.setMessage("Not authorized to delete dataproduct.");
					GenericMessage errorMessage = new GenericMessage();
					errorMessage.addErrors(notAuthorizedMsg);
					log.info("DataProduct with id {} cannot be deleted. User not authorized", id);
					return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
				}
			} else {
				MessageDescription invalidMsg = new MessageDescription("No dataProduct with the given id found");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(invalidMsg);
				log.info("No dataProduct with the given id {} found.", id);
				return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			log.error("Failed to delete dataProduct with id {} , due to internal error.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


    @ApiOperation(value = "Get all available dataproducts.", nickname = "getAll", notes = "Get all dataproducts. This endpoints will be used to get all valid available dataproduct records.", response = DataProductCollection.class, tags={ "dataproducts", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = DataProductCollection.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/dataproducts",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<DataProductCollection> getAll(
		    @ApiParam(value = "Filtering dataproduct based on publish state. Draft or published, values true or false") @Valid @RequestParam(value = "published", required = false) Boolean published,
			@ApiParam(value = "List of IDs of ART of dataproduct, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "art", required = false) String art,
			@ApiParam(value = "List of IDs of Carla Functions of dataproduct, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "carlafunction", required = false) String carlafunction,
			@ApiParam(value = "List of IDs of Platform of dataproduct, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "platform", required = false) String platform,
			@ApiParam(value = "List of IDs of Frontend-Tools of dataproduct, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "frontendTools", required = false) String frontendTool,
			@ApiParam(value = "List of Short IDs of Product Owner of dataproduct, seperated by comma. Example 1,2,3") @Valid @RequestParam(value = "productOwner", required = false) String productOwner,
    		@ApiParam(value = "page number from which listing of dataproducts should start.") @Valid @RequestParam(value = "offset", required = false) Integer offset,
    		@ApiParam(value = "page size to limit the number of dataproducts.") @Valid @RequestParam(value = "limit", required = false) Integer limit,
    		@ApiParam(value = "Sort dataproducts by a given variable.", allowableValues = "dataProductName, dataProductId") @Valid @RequestParam(value = "sortBy", required = false) String sortBy,
    		@ApiParam(value = "Sort dataproducts based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder ){
    	try {
			DataProductCollection dataProductCollection = new DataProductCollection();

			int defaultLimit = 10;
			if (offset == null || offset < 0)
				offset = 0;
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			if (sortOrder != null && !sortOrder.equals("asc") && !sortOrder.equals("desc")) {
				return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
			}
			if (sortOrder == null) {
				sortOrder = "asc";
			}

			String[] productOwners = null;
			List<String> productOwnerList = new ArrayList<>();
			if (productOwner != null && !"".equals(productOwner))
				productOwners = productOwner.split(",");
			if (productOwners != null && productOwners.length > 0)
				productOwnerList = Arrays.asList(productOwners);

			String[] arts = null;
			List<String> artsList = new ArrayList<>();
			if (art != null && !"".equals(art))
				arts = art.split(",");
			if (arts != null && arts.length > 0)
				artsList = Arrays.asList(arts);
		
			String[] carlafunctions = null;
			List<String> carlafunctionsList = new ArrayList<>();
			if (carlafunction != null && !"".equals(carlafunction))
				carlafunctions = carlafunction.split(",");
			if (carlafunctions != null && carlafunctions.length > 0)
				carlafunctionsList = Arrays.asList(carlafunctions);

			String[] platforms = null;
			List<String> platformsList = new ArrayList<>();
			if (platform != null && !"".equals(platform))
				platforms = platform.split(",");
			if (platforms != null && platforms.length > 0)
				platformsList = Arrays.asList(platforms);

			String[] frontendTools = null;
			List<String> frontendToolsList = new ArrayList<>();
			if (frontendTool != null && !"".equals(frontendTool))
				frontendTools = frontendTool.split(",");
			if (frontendTools != null && frontendTools.length > 0)
				frontendToolsList = Arrays.asList(frontendTools);

			String recordStatus = ConstantsUtility.OPEN;

			Long count = service.getCount(published, recordStatus, artsList, carlafunctionsList, platformsList, frontendToolsList, productOwnerList);
			if (count < offset)
				offset = 0;

			List<DataProductVO> dataProducts = service.getAllWithFilters(published, offset, limit, sortBy,
					sortOrder, recordStatus, artsList, carlafunctionsList, platformsList, frontendToolsList, productOwnerList);
			log.info("DataProducts fetched successfully");
			if (!ObjectUtils.isEmpty(dataProducts)) {
				dataProductCollection.setTotalCount(count.intValue());
				dataProductCollection.setRecords(dataProducts);
				return new ResponseEntity<>(dataProductCollection, HttpStatus.OK);
			} else {
				dataProductCollection.setTotalCount(count.intValue());
				return new ResponseEntity<>(dataProductCollection, HttpStatus.NO_CONTENT);
			}

		} catch (Exception e) {
			log.error("Failed to fetch dataProducts with exception {} ", e.getMessage());
			return new ResponseEntity<>(new DataProductCollection(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}


    @ApiOperation(value = "Get dataproduct for a given Id.", nickname = "getById", notes = "Get dataproduct for a given identifier. This endpoints will be used to get a dataproduct for a given identifier.", response = DataProductVO.class, tags={ "dataproducts", })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = DataProductVO.class),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/dataproducts/{id}",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.GET)
    public ResponseEntity<DataProductVO> getById(@ApiParam(value = "dataproduct ID to be fetched",required=true) @PathVariable("id") String id){
		DataProductVO dataProductVO = new DataProductVO();
		if (StringUtils.hasText(id)) {
			DataProductVO existingVO = service.getByUniqueliteral("dataProductId", id);
			if (existingVO != null && existingVO.getDataProductId() != null) {
				dataProductVO = existingVO;
			} else {
				dataProductVO =  service.getById(id);
			}
		}
		if (dataProductVO!=null
				&& dataProductVO.getId()!= null
				&& ConstantsUtility.OPEN.equalsIgnoreCase(dataProductVO.getRecordStatus())) {
			log.info("DataProduct {} fetched successfully", id);
			return new ResponseEntity<>(dataProductVO, HttpStatus.OK);
		}else {
			log.info("No DataProduct {} found", id);
			return new ResponseEntity<>(new DataProductVO(), HttpStatus.NO_CONTENT);
		}
    }


	@ApiOperation(value = "Get all available dataproducts owners.", nickname = "getAllDataproductOwners", notes = "Get all dataproducts owner. This endpoints will be used to get all valid available dataproduct owner records.", response = DataProductCollection.class, tags={ "dataproducts", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = DataProductCollection.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/dataproducts/owners",
			produces = { "application/json" },
			consumes = { "application/json" },
			method = RequestMethod.GET)
	public ResponseEntity<DataProductOwnerCollection> getAllDataproductOwners(
			@ApiParam(value = "Filtering dataproduct based on publish state. Draft or published, values true or false") @Valid @RequestParam(value = "published", required = false) Boolean published,
			@ApiParam(value = "page number from which listing of dataproducts should start.") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of dataproducts.") @Valid @RequestParam(value = "limit", required = false) Integer limit,
			@ApiParam(value = "Sort dataproducts based on the given order, example asc,desc", allowableValues = "asc, desc") @Valid @RequestParam(value = "sortOrder", required = false) String sortOrder ){
		try {
			DataProductOwnerCollection dataProductCollection = new DataProductOwnerCollection();

			int defaultLimit = 10;
			if (offset == null || offset < 0)
				offset = 0;
			if (limit == null || limit < 0) {
				limit = defaultLimit;
			}
			if (sortOrder != null && !sortOrder.equals("asc") && !sortOrder.equals("desc")) {
				return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
			}
			if (sortOrder == null) {
				sortOrder = "asc";
			}

			String recordStatus = ConstantsUtility.OPEN;

			Long count = service.getCountOwners(published, recordStatus);
			if (count < offset)
				offset = 0;

			List<String> dataProducts = new ArrayList<String>();
			dataProducts = service.getAllWithDataProductOwners(published, offset, limit, sortOrder, recordStatus);
			log.info("DataProducts fetched successfully");
			if (!dataProducts.isEmpty()) {
				dataProductCollection.setTotalCount(count.intValue());
				dataProductCollection.setRecords(dataProducts);
				return new ResponseEntity<>(dataProductCollection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(dataProductCollection, HttpStatus.NO_CONTENT);
			}
		} catch (Exception e) {
			log.error("Failed to fetch dataProducts owner list with exception {} ", e.getMessage());
			return new ResponseEntity<>(new DataProductOwnerCollection(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "Request of Data product Access.", nickname = "requestAccess", notes = "Request of Data product Access.", response = DataProductResponseVO.class, tags = {"dataproducts",})
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success", response = DataProductResponseVO.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error")})
	@RequestMapping(value = "/dataproducts/{id}/datatransfer",
			produces = {"application/json"},
			consumes = {"application/json"},
			method = RequestMethod.POST)
	public ResponseEntity<DataProductResponseVO> requestAccess(
			@ApiParam(value = "dataproduct ID to fill the Provider form", required = true) @PathVariable("id") String id,
			@ApiParam(value = "Request Body that contains data required for updating the datatransfer consumer form", required = true)
			@Valid @RequestBody DataTransferConsumerRequestInfoVO dataTransferConsumerRequestVO) {
		DataProductVO existingDataProduct = null;
		DataProductResponseVO responseVO = new DataProductResponseVO();
		if (dataTransferConsumerRequestVO != null && id != null) {
			existingDataProduct = service.getById(id);
			if (existingDataProduct != null && ConstantsUtility.OPEN.equalsIgnoreCase(existingDataProduct.getRecordStatus())) {
				try {
					ProviderVO providerVO = new ProviderVO();
					providerVO = assembler.convertDatatransferProviderForm(existingDataProduct);
					providerVO.setDataTransferName(dataTransferConsumerRequestVO.getData().getDataTransferName());
					providerVO.setNotifyUsers(dataTransferConsumerRequestVO.getData().isNotifyUsers());

					// To call Data transfer provider API.
					DataTransferProviderResponseVO dataTransferProviderResponseVO = service.createDataTransferProvider(providerVO).getBody();
					if (dataTransferProviderResponseVO.getErrors() != null) {
						String errorMessage = dataTransferProviderResponseVO.getErrors().get(0).getMessage();
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage(errorMessage);
						messages.add(message);
						responseVO.setErrors(messages);
						log.error("Exception occurred:{} while creating Data transfer provider form for DataProduct ID {} ", errorMessage,
								id);
						return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
					} else {
						// To call Data transfer consumer API after successful of provider form.
						DataTransferConsumerRequestVO dataTransferConsumerRequestForm = new DataTransferConsumerRequestVO();
						dataTransferConsumerRequestForm = assembler.convertDatatransferConsumerForm(dataTransferConsumerRequestVO);
						dataTransferConsumerRequestForm.getData().setId(dataTransferProviderResponseVO.getData().getId());

						DataTransferConsumerResponseVO dataTransferConsumerResponseVO = service.updateDataTransferConsumer(dataTransferConsumerRequestForm.getData()).getBody();
						if (dataTransferConsumerResponseVO.getErrors() != null) {
							String errorMessage = dataTransferConsumerResponseVO.getErrors().get(0).getMessage();
							List<MessageDescription> messages = new ArrayList<>();
							MessageDescription message = new MessageDescription();
							message.setMessage(errorMessage);
							messages.add(message);
							responseVO.setErrors(messages);
							log.error("Exception occurred:{} while creating Data transfer consumer form for DataProduct ID {} ", errorMessage,
									id);
							return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
						} else {
							ArrayList dataTransferAssociated = new ArrayList<>();
							DatatransfersAssociatedVO datatransfersAssociatedVO = new DatatransfersAssociatedVO();
							datatransfersAssociatedVO.setId(dataTransferProviderResponseVO.getData().getId());
							datatransfersAssociatedVO.setDatatransferId(dataTransferProviderResponseVO.getData().getDataTransferId());
							datatransfersAssociatedVO.setDatatrandferName(dataTransferProviderResponseVO.getData().getDataTransferName());
							dataTransferAssociated.add(datatransfersAssociatedVO);
							existingDataProduct.getDatatransfersAssociated().add(datatransfersAssociatedVO);

							DataProductVO vo = service.updateByID(existingDataProduct);
							if (vo != null && vo.getId() != null) {
								responseVO.setData(vo);
								log.info("for id {} request access has been created successfully", id);
								return new ResponseEntity<>(responseVO, HttpStatus.OK);
							} else {
								List<MessageDescription> messages = new ArrayList<>();
								MessageDescription message = new MessageDescription();
								message.setMessage("Failed to create request access due to internal error");
								messages.add(message);
								responseVO.setErrors(messages);
								log.error("id {} , failed to update", id);
								return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
					}
				} catch (Exception e) {
					log.error("Exception occurred:{} while creating request access for DataProduct ID {} ", e.getMessage(),
							id);
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage(e.getMessage());
					messages.add(message);
					responseVO.setErrors(messages);
					return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				log.error("DataProduct with the id {} is not found", id);
				return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			}
		} else {
			log.error("DataProduct id and body is empty");
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	@ApiOperation(value = "Update an existing dataproduct.", nickname = "update", notes = "Update an existing dataproduct.", response = DataProductVO.class, tags = {"dataproducts",})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = DataProductVO.class),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error")})
	@RequestMapping(value = "/dataproducts",
			produces = {"application/json"},
			consumes = {"application/json"},
			method = RequestMethod.PUT)
	public ResponseEntity<DataProductResponseVO> update(
			@ApiParam(value = "Request Body that contains data required for updating a new dataproduct", required = true)
			@Valid @RequestBody DataProductRequestVO dataProductRequestVO) {
		DataProductVO existingVO = null;
		DataProductVO requestVO = dataProductRequestVO.getData();
		if (requestVO != null && requestVO.getId() != null) {
			String id = requestVO.getId();
			existingVO = service.getById(id);
			DataProductResponseVO responseVO = new DataProductResponseVO();

			if (existingVO != null
					&& ConstantsUtility.OPEN.equalsIgnoreCase(existingVO.getRecordStatus())
					&& existingVO.getId().equals(dataProductRequestVO.getData().getId())) {

				CreatedByVO requestUser = this.userStore.getVO();
				CreatedByVO createdBy = existingVO.getCreatedBy();
				DataProductTeamMemberVO nameUser = new DataProductTeamMemberVO();
				DataProductTeamMemberVO informationOwner = new DataProductTeamMemberVO();
				if (existingVO.getContactInformation() != null && existingVO.getContactInformation().getName() != null) {
					nameUser = existingVO.getContactInformation().getName();
				}
				if (existingVO.getContactInformation() != null && existingVO.getContactInformation().getInformationOwner() != null) {
					informationOwner =  existingVO.getContactInformation().getInformationOwner();
				}
				if (requestUser.getId().equalsIgnoreCase(createdBy.getId())
						|| (informationOwner.getShortId() != null && requestUser.getId().equalsIgnoreCase(informationOwner.getShortId()))
						|| (nameUser.getShortId() != null && requestUser.getId().equalsIgnoreCase(nameUser.getShortId()))
				) {
					existingVO.setDescription(requestVO.getDescription());

					// To update data productName.
					if (!requestVO.getDataProductName().equals(existingVO.getDataProductName())) {
						String uniqueProductName = requestVO.getDataProductName();
						List<DataProductVO> dataProductVOs = service.getExistingDataProduct(uniqueProductName, ConstantsUtility.OPEN);
						if (!ObjectUtils.isEmpty(dataProductVOs) && (dataProductVOs.size() > 0)) {
							responseVO.setData(dataProductVOs.get(0));
							List<MessageDescription> messages = new ArrayList<>();
							MessageDescription message = new MessageDescription();
							message.setMessage("DataProduct name already exists.");
							messages.add(message);
							responseVO.setErrors(messages);
							log.info("DataProduct name {} already exists, returning as BAD_REQUEST", uniqueProductName);
							return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
						} else {
							existingVO.setDataProductName(uniqueProductName);
						}
					}

					// throw error if product id is changed.
					if (requestVO.getDataProductId() != null && !existingVO.getDataProductId().equals(requestVO.getDataProductId())) {
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("DataProduct Id not allowed to edit.");
						messages.add(message);
						responseVO.setErrors(messages);
						log.info("DataProduct id is not allowed to be modified" + requestVO.getDataProductId());
						return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
					}
					existingVO.setHowToAccessText(requestVO.getHowToAccessText());
					existingVO.setCarLaFunction(requestVO.getCarLaFunction());
					existingVO.setDdx(requestVO.getDdx());
					existingVO.setKafka(requestVO.getKafka());
					existingVO.setOneApi(requestVO.getOneApi());
					existingVO.setPlatform(requestVO.getPlatform());
					existingVO.setFrontEndTools(requestVO.getFrontEndTools());
					existingVO.setAgileReleaseTrain(requestVO.getAgileReleaseTrain());
					existingVO.setCorporateDataCatalog(requestVO.getCorporateDataCatalog());
					// For is Publish the below condition.
					// 1) only "false" can become "true".
					// 2) There will be no "null".
					// 3) If "false" then "false".
					// 4) from "true" can not become "false".
					if (!existingVO.isIsPublish() && requestVO.isIsPublish()) {
						existingVO.setIsPublish(requestVO.isIsPublish());
						if(Objects.isNull(requestVO.getClassificationConfidentiality()) || 
								   Objects.isNull(requestVO.getContactInformation()) ||
								   Objects.isNull(requestVO.getPersonalRelatedData()) ||
								   Objects.isNull(requestVO.getTransnationalDataTransfer())||
								   Objects.isNull(requestVO.getDeletionRequirement()) || Objects.isNull(requestVO.getDeletionRequirement().getInsiderInformation())) {
							List<MessageDescription> messages = new ArrayList<>();
							MessageDescription message = new MessageDescription();
							message.setMessage("DataProduct cannot be created as user is not providing the values for one of the tabs.");
							messages.add(message);
							responseVO.setErrors(messages);		
							log.info("DataProduct {} cannot be created as user is not providing the values for one of the tabs", id);
									return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
								}
					}
					if(requestVO.isIsPublish()) {
						if(Objects.isNull(requestVO.getClassificationConfidentiality()) || 
						   Objects.isNull(requestVO.getContactInformation()) ||
						   Objects.isNull(requestVO.getPersonalRelatedData()) ||
						   Objects.isNull(requestVO.getTransnationalDataTransfer())||
						   Objects.isNull(requestVO.getDeletionRequirement()) || Objects.isNull(requestVO.getDeletionRequirement().getInsiderInformation())) {
							List<MessageDescription> messages = new ArrayList<>();
							MessageDescription message = new MessageDescription();
							message.setMessage("DataProduct cannot be created as user is not providing the values for one of the tabs.");
							messages.add(message);
							responseVO.setErrors(messages);
							log.info("DataProduct {} cannot be created as user is not providing the values for one of the tabs", id);
							return new ResponseEntity<>(responseVO, HttpStatus.BAD_REQUEST);
						}
					}
					existingVO.setNotifyUsers(requestVO.isNotifyUsers());
					existingVO.setOpenSegments(requestVO.getOpenSegments());
					existingVO.lastModifiedDate(new Date());
					existingVO.setModifiedBy(requestUser);
					existingVO.setContactInformation(requestVO.getContactInformation());
					existingVO.setClassificationConfidentiality(requestVO.getClassificationConfidentiality());
					existingVO.setPersonalRelatedData(requestVO.getPersonalRelatedData());
					existingVO.setTransnationalDataTransfer(requestVO.getTransnationalDataTransfer());
					existingVO.setDeletionRequirement(requestVO.getDeletionRequirement());
					existingVO.setOpenSegments(requestVO.getOpenSegments());

					try {
						DataProductVO vo = service.updateByID(existingVO);
						if (vo != null && vo.getId() != null) {
							responseVO.setData(vo);
							log.info("id {} updated successfully", id);
							return new ResponseEntity<>(responseVO, HttpStatus.OK);
						} else {
							List<MessageDescription> messages = new ArrayList<>();
							MessageDescription message = new MessageDescription();
							message.setMessage("Failed to save due to internal error");
							messages.add(message);
							responseVO.setData(requestVO);
							responseVO.setErrors(messages);
							log.error("id {} , failed to update", id);
							return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
						}
					} catch (Exception e) {
						log.error("Exception occurred:{} while updating DataProduct {} ", e.getMessage(),
								requestVO.getDataProductName());
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage(e.getMessage());
						messages.add(message);
						responseVO.setData(requestVO);
						responseVO.setErrors(messages);
						return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					List<MessageDescription> messages = new ArrayList<>();
					MessageDescription message = new MessageDescription();
					message.setMessage("Not authorized to update the dataproduct.");
					messages.add(message);
					responseVO.setErrors(messages);
					log.info("DataProduct with id {} cannot be updated. User not authorized", id);
					return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
				}
			} else {
				return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			}
		} else {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}
}
