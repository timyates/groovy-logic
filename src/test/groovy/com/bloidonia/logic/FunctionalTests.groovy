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

    def "teacupo closure"() {
        setup:
            def logic = new Logic()
            def teacupo = logic.with {
                { v ->
                    conde( all( eq( 'tea', v ),
                                succeed() ),
                           all( eq( 'cup', v ),
                                succeed() ) )
                }
            }

        expect:
            with( logic ) {
                fresh { x, y, q ->
                    run( q, teacupo( q ) ) } == [ 'tea', 'cup' ]

                fresh { x, y, q ->
                    run( q, all( conde( all( teacupo( x ),
                                             eq( true, y ),
                                             succeed() ),
                                        all( eq( false, x ),
                                             eq( true, y ) ) ),
                                 eq( [ x, y ], q ) ) ) } == [ [ false, true ], [ 'tea', true ], [ 'cup', true ] ]
            }
    }

    def "eq checks"() {
        setup:
            def logic = new Logic()

        expect:
            with( logic ) {
                fresh { q, x, y, z, x_ ->
                    run( q, all( conde( all( eq( y, x ),
                                             eq( z, x_ ) ),
                                        all( eq( y, x_ ),
                                             eq( z, x ) ) ),
                                 eq( [ y, z ], q ) ) ) } == [ [ "_0", "_1" ], [ "_0", "_1" ] ]

                fresh { q, x, y, z, x_ ->
                    run( q, all( conde( all( eq( y, x ),
                                             eq( z, x_ ) ),
                                        all( eq( y, x_ ),
                                             eq( z, x ) ) ),
                                 eq( false, x ),
                                 eq( [ y, z ], q ) ) ) } == [ [ false, "_0" ], [ "_0", false ] ]

                fresh { q ->
                    def b = eq( false, q )
                    run( q, b ) } == [ false ]

                fresh { x, q ->
                    def b = all( eq( x, q ),
                                 eq( false, x ) )
                    run( q, b ) } == [ false ]

                fresh { x, y, q ->
                    run( q, eq( [ x, y ], q ) ) } == [ [ '_0', '_1' ] ]

                fresh { v, w, q ->
                    def (x, y) = [ v, w ]
                    run( q, eq( [ x, y ], q ) ) } == [ [ '_0', '_1' ] ]

                fresh { q, r, s ->
                    run( q, all( eq( [ r, s ], [ q, q ] ), eq( true, q ) ) ) } == [ true ]

                fresh { q ->
                    run( q, fresh { r -> eq( r, false ) } ) } == [ '_0' ]
            }
    }

    def "listo tests"() {
        setup:
            def logic = new Logic()
        expect:
            with( logic ) {
                fresh { q ->
                    run( q, listo( buildList( [ 'a', 'b', q, 'd' ] ) ) ) } == [ '_0' ]
                    
                fresh { q ->
                    run( 5, q, listo( [ 'a', [ 'b', [ 'c', q ] ] ] ) ) }   == [ buildList( [] ),
                                                                                buildList( [ '_0' ] ),
                                                                                buildList( [ '_0', '_1' ] ),
                                                                                buildList( [ '_0', '_1', '_2' ] ),
                                                                                buildList( [ '_0', '_1', '_2', '_3' ] ) ]
            }
    }

    // Does the list contain 2 elements
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

    def "membero test"() {
        setup:
            def logic = new Logic()
        expect:
            with( logic ) {
                // Find all q that are a member of [ 1, 2, 3 ] and [ 2, 3, 4 ]
                fresh { q ->
                    run( q, membero( q, buildList( [ 1, 2, 3 ] ) ),
                            membero( q, buildList( [ 2, 3, 4 ] ) ) ) } == [ 2, 3 ]
            }
    }

}