import unittest

from ..template_module import module_method_1


class TemplateModuleTestCase(unittest.TestCase):

    def test_sunshine(self):
        self.assertEqual(module_method_1(1, 2), 3, "Values do no match")
