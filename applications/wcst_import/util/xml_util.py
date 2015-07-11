class XMLUtil:

    @staticmethod
    def escape(xml_content):
        xml_content = xml_content.replace("<", "&lt;")
        xml_content = xml_content.replace(">", "&gt;")
        xml_content = xml_content.replace("&", "&amp;")
        xml_content = xml_content.replace("\"", "&quot;")
        return xml_content