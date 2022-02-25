import unittest

from ..subpackage.template_submodule_2 import submodule_2_method_1


class TemplateSubModule2TestCase(unittest.TestCase):

    def test_sunshine(self):
        self.assertEqual(submodule_2_method_1(1, 2), 2, "Values do no match")
