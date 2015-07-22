from lxml import etree
import urllib
# from master.error.runtime_exception import RuntimeException


class CoverageReader():
    def __init__(self, coverage_description_url):
        self.description_url = coverage_description_url

    def _read(self):
        try:
            xmlstr = urllib.urlopen(self.description_url).read()
            root = etree.fromstring(xmlstr)
            crs = root.xpath("//gml:Envelope", namespaces={"gml": "http://www.opengis.net/gml/3.2"})
            return crs
        except IOError as e:
            raise Exception(
                "Could not read the coverage description for url: " + self.description_url +
                ". Check that the url is accessible and try again.")


cov = CoverageReader("http://flanche.com/clim_clouds")
print cov._read()