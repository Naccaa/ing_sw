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

    def test_get_guidelines_response_status_code(self):
        res = get(f"{base_url}/guidelines")
        self.assertTrue(is_response_success(res.status_code), res.text)

    def test_get_guidelines_as_not_admin_response_status_code(self):
        s = login_as_not_admin()
        res = s.get(f"{base_url}/guidelines")
        self.assertTrue(is_response_success(res.status_code), res.text)

    def test_add_user(self):
        email = f"m{randrange(2**64)}"
        res = post(f"{base_url}/users", json={"email": email, "fullname": "f", "phone_number": "1", "password":"p"})
        self.assertTrue(is_response_success(res.status_code), res.text)

    def test_add_user(self):
        email = f"m{randrange(2**64)}"
        user_json = {"email": email, "fullname": "f", "phone_number": "1", "password":"p"}
        post_res = post(f"{base_url}/users", json=user_json)
        user_id = loads(post_res.text)['data']['user_id']
        token = loads(post(f"{base_url}/sessions",json={"email":email,"password":"p"}).text)['data']['session_token']
        s = Session()
        s.headers={"Authorization": f"Bearer {token}"}
        get_res = s.get(f"{base_url}/users/{user_id}")
        # print(f'{get_res.text = }')
        get_res_json = loads(get_res.text)['data']
        keys = ['email', 'fullname', 'phone_number']
        self.assertListEqual([user_json[k] for k in keys], [get_res_json[k] for k in keys])


if __name__ == "__main__":
    unittest.main()