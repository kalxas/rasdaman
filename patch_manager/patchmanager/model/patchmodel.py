import re


class PatchModel:
    __FROM_KEY = "From:"
    __from = None

    __DATE_KEY = "Date:"
    __date = None

    __SUBJECT_KEY = "Subject:"
    __subject = None

    __SUMMARY_KEY = "Summary:"
    __summary = None

    __TEST_PLAN_KEY = "Test Plan:"
    __test_plan = None

    __REVIEWERS_KEY = "Reviewers:"
    __reviewers = None

    __REVIEW_URL_KEY = "Differential Revision:"
    __review_url = None

    def __init__(self, patchText):
        self.__parsePatch(patchText.splitlines(True))

    def getFrom(self):
        return self.__from

    def getDate(self):
        return self.__date

    def getSummary(self):
        return self.__summary

    def getSubject(self):
        return self.__subject

    def getReviewers(self):
        return self.__reviewers

    def getReviewUrl(self):
        return self.__review_url

    def getTestPlan(self):
        return self.__test_plan

    def isSubjectValid(self):
        r = re.compile(r'\[PATCH[^\]]*\] +ticket:[0-9]+ .+', re.DOTALL)
        match = r.match(self.__subject)
        return match is not None

    def isFromValid(self):
        return self.__from

    def __parsePatch(self, patchText):
        for line in patchText:
            if line.startswith(self.__DATE_KEY):
                self.__date = self.__extract_line_content(line, self.__DATE_KEY)
            elif line.startswith(self.__FROM_KEY):
                self.__from = self.__extract_line_content(line, self.__FROM_KEY)
            elif line.startswith(self.__SUBJECT_KEY):
                self.__subject = self.__extract_line_content(line, self.__SUBJECT_KEY)
            elif line.startswith(self.__SUMMARY_KEY):
                self.__summary = self.__extract_line_content(line, self.__SUMMARY_KEY)
            elif line.startswith(self.__TEST_PLAN_KEY):
                self.__test_plan = self.__extract_line_content(line, self.__TEST_PLAN_KEY)
            elif line.startswith(self.__REVIEW_URL_KEY):
                self.__review_url = self.__extract_line_content(line, self.__REVIEW_URL_KEY)
            elif line.startswith(self.__REVIEWERS_KEY):
                self.__reviewers = self.__extract_line_content(line, self.__REVIEWERS_KEY)

    @staticmethod
    def __extract_line_content(line, key):
        if line is not None and line.startswith(key):
            return line[len(key):].strip()
        return None
