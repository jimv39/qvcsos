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
package com.qumasoft.qvcslib;

/**
 * Transaction begin request data.
 * @author Jim Voris
 */
public class ClientRequestTransactionBeginData extends ClientRequestClientData {
    private static final long serialVersionUID = -2386671692952002216L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.TRANSACTION_ID
    };

    /**
     * Creates a new instance of ClientRequestTransactionBeginData.
     */
    public ClientRequestTransactionBeginData() {
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.BEGIN_TRANSACTION;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
