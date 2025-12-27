#!/usr/bin/env python3

from requests import *
from json import *
from base64 import b64encode
import unittest
from random import seed, randint, randrange
seed(1234)

# https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
def is_response_success(status_code):
    return 200 <= status_code < 300

response_status_code_unauthorized = 401
protocol = 'http'
host = 'dev_server'
port = 5000
base_url = f"{protocol}://{host}:{port}"

def login_as_not_admin():
    email = f"m{randrange(2**64)}"
    res = post(f"{base_url}/users", json={"email": email, "fullname": "f", "phone_number": "1", "password":"p"})
    # print(f'{res.text = }')
    token = loads(post(f"{base_url}/sessions",json={"email":email,"password":"p"}).text)['data']['session_token']
    s = Session()
    s.headers={"Authorization": f"Bearer {token}"}
    return s


class TestEndpoints(unittest.TestCase):
    def setUp(self):
        pass

    def test_get_guidelines(self):
        res = get(f"{base_url}/guidelines")
        self.assertTrue(is_response_success(res.status_code), res.text)

    def test_get_guidelines_as_not_admin(self):
        s = login_as_not_admin()
        res = s.get(f"{base_url}/guidelines")
        self.assertTrue(is_response_success(res.status_code), res.text)


if __name__ == "__main__":
    unittest.main()