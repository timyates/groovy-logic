/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloidonia.logic

class UtilTests extends spock.lang.Specification {
    def "buildNumber tests"() {
        expect:
            result == new Logic().with {
                buildNumber( n )
            }
        where:
            n     | result
            5     | [ 1, [ 0, [ 1, [] ] ] ]
            7     | [ 1, [ 1, [ 1, [] ] ] ]
            9     | [ 1, [ 0, [ 0, [ 1, [] ] ] ] ]
            17290 | [ 0, [ 1, [ 0, [ 1, [ 0, [ 0, [ 0, [ 1, [ 1, [ 1, [ 0, [ 0, [ 0, [ 0, [ 1, [] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ]
    }

    def "unpackNumber tests"() {
        expect:
            n == new Logic().with {
                unpackNumber( list )
            }
        where:
            n     | list
            5     | [ 1, [ 0, [ 1, [] ] ] ]
            7     | [ 1, [ 1, [ 1, [] ] ] ]
            9     | [ 1, [ 0, [ 0, [ 1, [] ] ] ] ]
            17290 | [ 0, [ 1, [ 0, [ 1, [ 0, [ 0, [ 0, [ 1, [ 1, [ 1, [ 0, [ 0, [ 0, [ 0, [ 1, [] ] ] ] ] ] ] ] ] ] ] ] ] ] ] ]
    }

    def "buildList tests"() {
        expect:
            result == new Logic().with {
                buildList( n )
            }
        where:
            n           | result
            []          | []
            [ 1 ]       | [ 1, [] ]
            [ 1, 2 ]    | [ 1, [ 2, [] ] ]
            [ 1, 2, 3 ] | [ 1, [ 2, [ 3, [] ] ] ]
    }

    def "unpackNumber tests"() {
        expect:
            result == new Logic().with {
                unpackList( list )
            }
        where:
            result      | list
            []          | []
            [ 1 ]       | [ 1, [] ]
            [ 1, 2 ]    | [ 1, [ 2, [] ] ]
            [ 1, 2, 3 ] | [ 1, [ 2, [ 3, [] ] ] ]
    }

}