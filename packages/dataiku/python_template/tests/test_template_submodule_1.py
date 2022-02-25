import unittest

from ..subpackage.template_submodule_1 import submodule_1_method_1


class TemplateSubModule1TestCase(unittest.TestCase):

    def test_sunshine(self):
        self.assertEqual(submodule_1_method_1(1, 2), -1, "Values do no match")
