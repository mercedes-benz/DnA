import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.daimler.data.dto.solution.TransparencyVO;
import org.springframework.beans.factory.annotation.Value;
	
	@Value("${jupyternotebook.sleepTime}")
	private String sleepTime;
		}
	}

	@Override
	@ApiOperation(value = "Get number of notebooks.", nickname = "getNumberOfNotebooks", notes = "Get number of notebooks.", response = TransparencyVO.class, tags = {
			"notebooks", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of succes or failure", response = TransparencyVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/notebooks/transparency", method = RequestMethod.GET)
	public ResponseEntity<TransparencyVO> getNumberOfNotebooks() {
		try {
			TransparencyVO transparencyVO = new TransparencyVO();
			Integer count = notebookService.getTotalNumberOfNotebooks();
			transparencyVO.setCount(count);
			LOGGER.info("Returning notebook count successfully");
			return new ResponseEntity<>(transparencyVO, HttpStatus.OK);
		}catch (Exception e) {
			LOGGER.error("Failed while fetching notebook count with exception {}", e.getMessage());
			return new ResponseEntity<>(new TransparencyVO(), HttpStatus.INTERNAL_SERVER_ERROR);
				int threadSleepTime = Integer.parseInt(sleepTime);
				Thread.sleep(threadSleepTime);
