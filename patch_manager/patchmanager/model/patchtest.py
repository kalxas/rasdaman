import unittest
from patchmodel import PatchModel


class TestPatch(unittest.TestCase):
    __PATCH_BODY = "From 5ee595ae4c3734dfa50cfabe7535b5b56324d93f Mon Sep 17 00:00:00 2001\n" \
                   "From: %s\n" \
                   "Date: %s\n" \
                   "Subject: %s\n" \
                   "\n" \
                   "Summary: %s\n" \
                   "\n" \
                   "Test Plan: %s\n" \
                   "\n" \
                   "Reviewers: %s\n" \
                   "\n" \
                   "Differential Revision: %s\n"

    __expected_from = "John Doe <john@doe.com"
    __expected_date = "Thu, 17 Dec 2015 12:24:52 +0100"
    __expected_subject = "[PATCH] ticket:1 - unit test patch parse"
    __expected_summary = "Unit test patch summary"
    __expected_test_plan = "Unit test plan"
    __expected_reviewrs = "jdoe"
    __expected_revision_url = "http://unittest.review.com/D1"

    __invalid_subject = "unit test patch parse"
    __invalid_from = ""

    def test_valid_patch(self):
        patch = PatchModel(self.__PATCH_BODY % (
            self.__expected_from, self.__expected_date, self.__expected_subject, self.__expected_summary,
            self.__expected_test_plan, self.__expected_reviewrs, self.__expected_revision_url))

        self.assertEqual(self.__expected_from, patch.getFrom())
        self.assertEqual(self.__expected_date, patch.getDate())
        self.assertEqual(self.__expected_subject, patch.getSubject())
        self.assertEqual(self.__expected_summary, patch.getSummary())
        self.assertEqual(self.__expected_test_plan, patch.getTestPlan())
        self.assertEqual(self.__expected_reviewrs, patch.getReviewers())
        self.assertEqual(self.__expected_revision_url, patch.getReviewUrl())
        self.assertTrue(patch.isSubjectValid())
        self.assertTrue(patch.isFromValid())

    def test_invalid_subject(self):
        patch = PatchModel(self.__PATCH_BODY % (
            self.__invalid_from, "", self.__invalid_subject, "", "", "", ""))

        self.assertFalse(patch.isSubjectValid())
        self.assertFalse(patch.isFromValid())

if __name__ == '__main__':
    unittest.main()
