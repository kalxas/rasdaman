class RuntimeException(Exception):
    def __init__(self, validation_message):
        """
        Class to take care of runtime exceptions
        :param validation_message: the failed runtime message
        """
        self.validation_message = validation_message

    def __str__(self):
        return "RUNTIME ERROR: " + self.validation_message