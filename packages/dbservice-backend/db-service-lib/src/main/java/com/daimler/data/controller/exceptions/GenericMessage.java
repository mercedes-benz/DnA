package com.daimler.data.controller.exceptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GenericMessage implements Serializable {

	private static final long serialVersionUID = -4670111514075856100L;

	private String success;
	private List<MessageDescription> errors;
	private List<MessageDescription> warnings;

	public GenericMessage() {
	}

	public GenericMessage(String success) {
		super();
		this.success = success;
	}

	public GenericMessage(String success, List<MessageDescription> errors, List<MessageDescription> warnings) {
		super();
		this.success = success;
		this.errors = errors;
		this.warnings = warnings;
	}

	public List<MessageDescription> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<MessageDescription> warnings) {
		this.warnings = warnings;
	}

	public List<MessageDescription> getErrors() {
		return errors;
	}

	public void setErrors(List<MessageDescription> errors) {
		this.errors = errors;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GenericMessage genericMessage = (GenericMessage) o;
		return Objects.equals(this.success, genericMessage.success)
				&& Objects.equals(this.errors, genericMessage.errors)
				&& Objects.equals(this.warnings, genericMessage.warnings);
	}

	public GenericMessage addErrors(MessageDescription... customErrors) {
		if (this.errors == null) {
			this.errors = new ArrayList<MessageDescription>();
		}
		if (customErrors != null) {
			List<MessageDescription> errorsList = Arrays.asList(customErrors);
			this.errors.addAll(errorsList);
		}
		return this;
	}

	public GenericMessage addWarnings(MessageDescription... customWarnings) {
		if (this.warnings == null) {
			this.warnings = new ArrayList<MessageDescription>();
		}
		if (customWarnings != null) {
			List<MessageDescription> warningsList = Arrays.asList(customWarnings);
			this.warnings.addAll(warningsList);
		}
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(success, errors, warnings);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class GenericMessage {\n");

		sb.append("    success: ").append(toIndentedString(success)).append("\n");
		sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
		sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

}

