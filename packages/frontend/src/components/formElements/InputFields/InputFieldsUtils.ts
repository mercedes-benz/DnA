class InputFieldsUtils {
  public static resetErrors(query?: string): void {
    const underElement = query ? document.querySelector(query) : document;
    const inputFieldGroups: NodeListOf<Element> = underElement.querySelectorAll('.input-field-group.error');
    Array.from(inputFieldGroups).forEach((inputFieldGroup) => {
      inputFieldGroup.classList.remove('error');
      const errorMessage = inputFieldGroup.querySelector('.error-message');
      if (errorMessage) {
        errorMessage.parentNode.removeChild(errorMessage);
      }
    });
  }
}

export default InputFieldsUtils;
