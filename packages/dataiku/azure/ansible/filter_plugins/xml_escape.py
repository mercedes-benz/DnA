import json


def xml_escape(xml_text):
    escaped_text = (xml_text
                    .replace('"', '\\"'))

    return escaped_text


class FilterModule(object):
    def filters(self):
        return {
            'xml_escape': xml_escape
        }