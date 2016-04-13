import requests

class JenkinsApi:
    __BUILD_COMMAND = "/buildWithParameters?delay=0sec";
    __JSON_API = "/api/json?depth=1";
    __PATCH_ID_KEY = "PATCH_ID";
    __PATCH_BRANCH = "PATCH_BRANCH"
    __JOB_URL_PATH = "/job/"
    __TEST_JOBS_THRESHOLD = 15;
    __AUTOMATIC_JOBS_THRESHOLD = 5;

    __jenkins_user = None
    __jenkins_passwd = None
    __jenkins_url = None
    __log = None

    def __init__(self, url, user, passwd, log=None):
        self.__jenkins_user = user;
        self.__jenkins_passwd = passwd;
        self.__jenkins_url = url;
        self.__log = log;

    def get_test_patch_map(self, job_name):
        r = self.__send_jenkins_request(job_name, self.__JSON_API)

        result = dict()

        if r is None:
            return result

        reqBuilds = r.json()
        for builds in reqBuilds['builds']:
            patch_id = None
            build_status = builds['result']
            building = builds['building']
            url = builds['url']
            for value in builds['actions']:
                if 'parameters' in value:
                    for parameter in value['parameters']:
                        if parameter['name'] == 'PATCH_ID':
                            patch_id = parameter['value']
            if not patch_id is None and patch_id.isnumeric():
                result[int(patch_id)] = {'buildStatus': build_status, 'building': building, 'url': url}

        return self.__get_last_n_dict_elemets(result, self.__TEST_JOBS_THRESHOLD);

    def get_automatic_build_map(self, job_name):
        result = dict()

        r = self.__send_jenkins_request(job_name, self.__JSON_API);
        if r is None:
            return result

        for build in r.json()['builds']:
            buildId = build['number']
            building = build['building']
            status = build['result']
            status_color = 'red'
            if status == 'SUCCESS':
                status_color = 'green'
            changes = build['changeSet']['items']
            result[int(buildId)] = {
                "buildId": buildId,
                "building": building,
                "status": status,
                "changes": changes,
                "status_color": status_color
            }

        return self.__get_last_n_dict_elemets(result, self.__AUTOMATIC_JOBS_THRESHOLD)

    def test_patch(self, patch_id, job_name, branch="master"):
        params = {self.__PATCH_ID_KEY: patch_id, self.__PATCH_BRANCH: branch};
        r = self.__send_jenkins_request(job_name, self.__BUILD_COMMAND, params)
        if not self.__log is None:
            self.__log.debug(
                "Test request for patch id '" + str(patch_id) + "': status code (" + str(r.status_code) + ")");

    def __send_jenkins_request(self, job, apiCall, params=None):
        self.__log.debug(self.__jenkins_url + self.__JOB_URL_PATH + job + apiCall)
        r = None
        try:
         r = requests.post(self.__jenkins_url + self.__JOB_URL_PATH + job + apiCall, params,
                          auth=(self.__jenkins_user, self.__jenkins_passwd));
         if r.status_code != 200:
            if not self.__log is None:
                self.__log.debug("Request failed with status: " + str(r.status_code))
        except:
         pass

        return r

    def __slice_dict_by_threshold_key(self, dictionary, threshold):
        result = dict()

        for key in dictionary.keys():
            if key >= threshold:
                result[key] = dictionary[key]

        return result

    def __get_last_n_dict_elemets(self, dictionary, n):
        keys = sorted(dictionary.keys());
        length = len(keys);
        if length is 0 or length < n or n <= 0:
            return dictionary

        threshold = keys[len(keys) - n];
        return self.__slice_dict_by_threshold_key(dictionary, threshold);
