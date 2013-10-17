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

class FunctionalTests extends spock.lang.Specification {
    def exec = { n=null, fn ->
        def vars = (1..fn.maximumNumberOfParameters).collect { fresh() }
        run( n, vars[ 0 ], fn( *vars ) )
    }

    def "base tests"() {
        setup:
            def logic = new Logic()
            exec.delegate = logic
        expect:
            with( logic ) {
                exec( { q -> eq( true, q ) } )                                == [ true ]

                exec( { q -> succeed() } )                                    == [ '_0' ]

                exec( { q -> fail() } )                                       == []

                exec( { q -> all( succeed(), eq( 'corn', q ) ) } )            == [ 'corn' ]

                exec( { q, x -> all( eq( true, x ), eq( true, q ) ) } )       == [ true ]

                exec( { q, x, y -> eq( [ x, y ], q ) } )                      == [ [ '_0', '_1' ] ]

                exec( { q, x -> eq( x == q, q ) } )                           == [ false ]

                exec( { q -> conde( all( fail(), succeed() ),
                                    all( succeed(), fail() ) ) } )            == []

                exec( { q -> conde( all( fail(), fail() ),
                                    all( succeed(), succeed() ) ) } )         == [ '_0' ]

                exec( { q -> conde( all( succeed(), succeed() ),
                                    all( fail(), fail() ) ) } )               == [ '_0' ]

                exec( { q -> conde( all( eq( 'olive', q ), succeed() ),
                                    all( eq( 'oil', q ), succeed() ) ) } )    == [ 'olive', 'oil' ]

                exec( 1, { q -> conde( all( eq( 'olive', q ), succeed() ),
                                       all( eq( 'oil', q ), succeed() ) ) } ) == [ 'olive' ]

                exec( { q -> conde( all( eq( 'virgin', q ), fail() ),
                                    all( eq( 'olive', q ), succeed() ),
                                    all( succeed(), succeed() ),
                                    all( eq( 'oil', q ), succeed() ) ) } )    == [ 'olive', '_0', 'oil' ]

                exec( { q -> conde( all( eq( 'olive', q ), succeed() ),
                                    all( succeed(), succeed() ),
                                    all( eq( 'oil', q ), succeed() ) ) } )    == [ 'olive', '_0', 'oil' ]

                exec( { q -> conde( all( eq( 'extra', q ), succeed() ),
                                    all( eq( 'virgin', q ), fail() ),
                                    all( eq( 'olive', q ), succeed() ),
                                    all( eq( 'oil', q ), succeed() ) ) } )    == [ 'extra', 'olive', 'oil' ]

                exec( { q, x, y -> conde( all( eq( 'split', x ),
                                               eq( 'pea', y ),
                                               eq( [ x, y ], q ) ) ) } )      == [ [ 'split', 'pea' ] ]

                exec( { q, x, y -> all( all( conde( all( eq( 'split', x ),
                                                         eq( 'pea', y ) ),
                                                    all( eq( 'navy', x ),
                                                         eq( 'bean', y ) ) ) ),
                                        eq( [ x, y ], q ) ) } )               == [ [ 'split', 'pea' ], [ 'navy', 'bean' ] ]

                exec( { q, x, y -> all( conde( all( eq( 'split', x ),eq( 'pea', y ) ),
                                               all( eq( 'navy', x ), eq( 'bean', y ) ) ),
                                        eq( [ x, y, 'soup' ], q ) ) } )       == [ [ 'split', 'pea', 'soup' ], [ 'navy', 'bean', 'soup' ] ]
            }
    }

    def "pairo tests"() {
        setup:
            def logic = new Logic()
        expect:
            with( logic ) {
                fresh { q ->
                    run( q, all( pairo( [ q, q ] ),
                                 eq( true, q ) ) ) } == [ true ]
                fresh { q ->
                    run( q, all( pairo( [] ),
                                 eq( true, q ) ) ) } == []
            }
    }
}