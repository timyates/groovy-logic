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

class MathTests extends spock.lang.Specification {
    // Test the full adder logic
    def "fullAddero tests"() {
        setup:
            def logic = new Logic()
        when:
            def resultA = [ [ 1, 1 ] ]
            def resultB = [ [ 0, 0, 0, 0, 0 ], [ 1, 0, 0, 1, 0 ], [ 0, 1, 0, 1, 0 ],
                            [ 1, 1, 0, 0, 1 ], [ 0, 0, 1, 1, 0 ], [ 1, 0, 1, 0, 1 ],
                            [ 0, 1, 1, 0, 1 ], [ 1, 1, 1, 1, 1 ] ]
        then:
            with( logic ) {
                resultA == fresh { r, c, s ->
                    run( s, all( fullAddero( 1, 1, 1, r, c ),
                                 eq( [ r, c ], s ) ) )
                }
                resultB == fresh { r, c, s, b, x, y ->
                    run( s, all( fullAddero( b, x, y, r, c ),
                                 eq( [ b, x, y, r, c ], s ) ) )
                }
            }
    }

    // Test for positive numbers
    def "poso tests"() {
        when:
            def logic = new Logic()
        then:
            with( logic ) {
                [ true ] == fresh { q ->
                    run( q, all( poso( [ 0, [ 1, [ 1, [] ] ] ] ),
                                 eq( true, q ) ) )
                }

                [ true ] == fresh { q ->
                    run( q, all( poso( [ 1, [] ] ),
                                 eq( true, q ) ) )
                }

                [] == fresh { q ->
                    run( q, all( poso( [] ),
                                 eq( true, q ) ) )
                }

                [ [ '_0', '_1' ] ] == fresh { r ->
                    run( r, poso( r ) )
                }
            }
    }

    // Test for numbers greater than 1
    def "gt1o tests"() {
        when:
            def logic = new Logic()
        then:
            with( logic ) {
                // 2 is greater than 1
                [ true ] == fresh { q ->
                    run( q, all( gt1o( buildNumber( 2 ) ),
                                 eq( true, q ) ) )
                }

                // 1 is not greater than 1
                [] == fresh { q ->
                    run( q, all( gt1o( logic.buildNumber( 1 ) ),
                                 eq( true, q ) ) )
                }

                // 0 is not greater than 1
                [] == fresh { q ->
                    run( q, all( gt1o( logic.buildNumber( 0 ) ),
                                 eq( true, q ) ) )
                }

                [ [ '_0', [ '_1', '_2' ] ] ] == fresh { q ->
                    run( q, gt1o( q ) )
                }
            }
    }

    // Multiplication
    @spock.lang.Unroll("multo( #a, #b ) == #result")
    def "multo tests"() {
        setup:
            def logic = new Logic()
        expect:
            with( logic ) {
                [ buildNumber( result ) ] == fresh { p ->
                    run( p, multo( buildNumber( a ), buildNumber( b ), p ) )
                }
            }
        where:
            a     | b    | result
            2     | 2    | 4
            4     | 4    | 16
            8     | 8    | 64
            16    | 16   | 256
            32    | 32   | 1024
            1     | 32   | 32
            32    | 1    | 32
            0     | 32   | 0
            3     | 3    | 9
            3     | 5    | 15
            5     | 31   | 155
            5     | 63   | 315
            5     | 64   | 320
            7     | 31   | 217
            7     | 63   | 441
    }

    // Division
    @spock.lang.Unroll("divo( #a, #b ) == #result")
    def "divo tests"() {
        setup:
            def logic = new Logic()
        expect:
            with( logic ) {
                result == fresh { q ->
                    run( q, divo( buildNumber( a ), buildNumber( b ), q, buildNumber( 0 ) ) )
                }
            }
        where:
            a     | b    | result
            4     | 2    | [ new Logic().buildNumber( 2 ) ]
            8     | 1    | [ new Logic().buildNumber( 8 ) ]
            8     | 2    | [ new Logic().buildNumber( 4 ) ]
            8     | 4    | [ new Logic().buildNumber( 2 ) ]
            1     | 1    | [ new Logic().buildNumber( 1 ) ]
            16    | 4    | [ new Logic().buildNumber( 4 ) ]
            16    | 3    | []
    }

    // Addition
    @spock.lang.Unroll("pluso( #a, #b ) == #result")
    def "pluso tests"() {
        setup:
            def logic = new Logic()
        expect:
            with( logic ) {
                [ buildNumber( result ) ] == fresh { p ->
                    run( p, pluso( buildNumber( a ), buildNumber( b ), p ) )
                }
            }
        where:
            a     | b    | result
            3     | 6    | 9
            4     | 4    | 8
            1     | 0    | 1
    }

    // Subtraction
    @spock.lang.Unroll("minuso( #a, #b ) == #result")
    def "minus tests"() {
        setup:
            def logic = new Logic()
        expect:
            with( logic ) {
                [ buildNumber( result ) ] == fresh { p ->
                    run( p, minuso( buildNumber( a ), buildNumber( b ), p ) )
                }
            }
        where:
            a     | b    | result
            6     | 3    | 3
            8     | 5    | 3
            6     | 6    | 0
    }

    def "logical pluso"() {
        when:
            def logic = new Logic()
        and:
            def result = logic.with {
                fresh { s, x, y ->
                    run( s, all( pluso( x, y, buildNumber( 5 ) ), // x plus y == 5
                                 eq( [ x, y ], s ) ) )            // and s == [ x, y ]
                }
            }
        then:
            with( logic ) {
                result == [ [ buildNumber( 5 ), buildNumber( 0 ) ],      // x = 5, y == 0
                            [ buildNumber( 0 ), buildNumber( 5 ) ],      // x = 0, y == 5
                            [ buildNumber( 1 ), buildNumber( 4 ) ],      // x = 1, y == 4
                            [ buildNumber( 4 ), buildNumber( 1 ) ],      // x = 4, y == 1
                            [ buildNumber( 3 ), buildNumber( 2 ) ],      // x = 3, y == 2
                            [ buildNumber( 2 ), buildNumber( 3 ) ] ]     // x = 2, y == 3
            }
    }

    def "check the adder"() {
        when:
            def logic = new Logic()
        then:
            with( logic ) {
                fresh { x, y, r, s ->
                    run( 3, s, all( addero( 0, x, y, r ),
                                    eq( [ x, y, r ], s ) ) )
                } == [ [ '_0',             [],               '_0'             ],
                       [ [],               [ '_0', '_1' ],   [ '_0', '_1' ]   ],
                       [ buildNumber( 1 ), buildNumber( 1 ), buildNumber( 2 ) ] ]

                fresh { s ->
                    run( s, genAddero( 1, buildNumber( 6 ), buildNumber( 3 ), s ) ) 
                } == [ buildNumber( 10 ) ]

                fresh { s ->
                    run( s, genAddero( 0, buildNumber( 6 ), buildNumber( 3 ), s ) ) 
                } == [ buildNumber( 9 ) ]
            }

    }
}