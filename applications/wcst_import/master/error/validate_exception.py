class RecipeValidationException(Exception):
    def __init__(self, validation_message):
        """
        Class to take care of validation exceptions
        :param validation_message: the failed validation message
        """
        self.validation_message = validation_message

    def __str__(self):
        return "VALIDATION ERROR: " + self.validation_message