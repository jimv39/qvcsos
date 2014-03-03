//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.webserver;

interface HttpConstants {

    /**
     * 2XX: generally "OK"
     */
    int HTTP_OK = 200;
    int HTTP_CREATED = 201;
    int HTTP_ACCEPTED = 202;
    int HTTP_NOT_AUTHORITATIVE = 203;
    int HTTP_NO_CONTENT = 204;
    int HTTP_RESET = 205;
    int HTTP_PARTIAL = 206;
    /**
     * 3XX: relocation/redirect
     */
    int HTTP_MULT_CHOICE = 300;
    int HTTP_MOVED_PERM = 301;
    int HTTP_MOVED_TEMP = 302;
    int HTTP_SEE_OTHER = 303;
    int HTTP_NOT_MODIFIED = 304;
    int HTTP_USE_PROXY = 305;
    /**
     * 4XX: client error
     */
    int HTTP_BAD_REQUEST = 400;
    int HTTP_UNAUTHORIZED = 401;
    int HTTP_PAYMENT_REQUIRED = 402;
    int HTTP_FORBIDDEN = 403;
    int HTTP_NOT_FOUND = 404;
    int HTTP_BAD_METHOD = 405;
    int HTTP_NOT_ACCEPTABLE = 406;
    int HTTP_PROXY_AUTH = 407;
    int HTTP_CLIENT_TIMEOUT = 408;
    int HTTP_CONFLICT = 409;
    int HTTP_GONE = 410;
    int HTTP_LENGTH_REQUIRED = 411;
    int HTTP_PRECON_FAILED = 412;
    int HTTP_ENTITY_TOO_LARGE = 413;
    int HTTP_REQ_TOO_LONG = 414;
    int HTTP_UNSUPPORTED_TYPE = 415;
    /**
     * 5XX: server error
     */
    int HTTP_SERVER_ERROR = 500;
    int HTTP_INTERNAL_ERROR = 501;
    int HTTP_BAD_GATEWAY = 502;
    int HTTP_UNAVAILABLE = 503;
    int HTTP_GATEWAY_TIMEOUT = 504;
    int HTTP_VERSION = 505;
}
