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

import groovy.transform.*

@CompileStatic
class Logic {
    class Var {}
    
    Map unify( u, v, Map s ) {
        u = walk( u, s )
        v = walk( v, s )
        if( u.is( v ) ) {
            return s
        }
        else if( u instanceof Var ) {
            if( v instanceof Var ) {
                return extS( u, v, s )
            }
            else {
                return extSCheck( u, v, s )
            }
        }
        else if( v instanceof Var ) {
            return extSCheck( v, u, s )
        }
        else if( u instanceof List && v instanceof List ) {
            if( u.size() != v.size() ) {
                return null
            }
            else if( u.size() == 0 ) {
                return s
            }
            else {
                [u,v].transpose().each { x, y ->
                    if( s != null ) {
                        s = unify( x, y, s )
                    }
                }
                return s
            }
        }
    }

    def walk( v, Map s ) {
        if( v instanceof Var && s.containsKey( v ) ) {
            return walk( s[ v ], s )
        }
        return v
    }

    Map extSCheck( x, v, Map s ) {
        occursCheck( x, v, s ) ? null : extS( x, v, s )
    }
    
    boolean occursCheck( x, v, Map s ) {
        v = walk( v, s )
        if( v instanceof Var ) {
            return x.is( v )
        }
        else if( v instanceof List ) {
            return v.find { vv -> occursCheck( x, vv, s ) } != null
        }
        return false
    }
    
    Map extS( x, v, Map s ) {
        s << [ (x): v ]
    }

    Map reifyS( v, Map s ) {
        v = walk( v, s )
        if( v instanceof Var ) {
            extS( v, reifyName( s.size() ), s )
        }
        else if( v instanceof List && v.size() > 0 ) {
            v.each { vv -> s = reifyS( vv, s ) }
            return s
        }
        return s
    }
        
    def reify( v, Map s ) {
        v = walkAll( v, s )
        walkAll( v, reifyS( v, [:] ) )
    }
    
    def walkAll( w, Map s ) {
        def v = walk( w, s )
        if( v instanceof List ) {
            return v.collect { vv -> walkAll( vv, s ) }
        }
        return v
    }
    
    String reifyName( int v ) {
        "_$v"
    }
    
    @CompileDynamic
    def mplus( ss, f ) {
        if( ss == null ) {
            return f()
        }
        else if( ss instanceof Closure ) {
            return { -> mplus( f(), ss ) }
        }
        else if( ss instanceof List ) {
            return [ ss[ 0 ], { -> mplus( ss[ 1 ](), f ) } ]
        }
        return [ ss, f ]
    }
    
    @CompileDynamic
    List take( n, f ) {
        def result = []
        while( n == null || n > 0 ) {
            def ss = f()
            if( ss == null ) {
                return result
            }
            else if( ss instanceof Closure ) {
                f = ss
            }
            else if( ss instanceof List ) {
                if( n ) n--
                result << ss[ 0 ]
                f = ss[ 1 ]
            }
            else {
                result << ss
                return result
            }
        }
        return result
    }
    
    @CompileDynamic
    def bind( ss, goal ) {
        if( ss == null ) {
            return null
        }
        else if( ss instanceof Closure ) {
            return { -> bind( ss(), goal ) }
        }
        else if( ss instanceof List ) {
            return mplus( goal( ss[ 0 ] ), { -> bind( ss[ 1 ](), goal ) } )
        }
        return goal( ss )
    }
    
    @CompileDynamic
    def mplusAll( goals, s ) {
        if( goals.size() == 1 ) {
            return goals[ 0 ]( s )
        }
        return mplus( goals[ 0 ]( s.clone() ), { -> mplusAll( goals[ 1..-1 ], s ) } )
    }
    
    def eq( u, v ) {
        { Map s ->
            s = unify( u, v, s )
            if( s == null ) {
                return null
            }
            return s
        }
    }
    
    def all( ...goals ) {
        goals = goals as List
        if( goals.size() == 0 ) {
            return succeed()
        }
        return { s ->
            goals.each { goal -> s = bind( s, goal ) }
            return s
        }
    }
    
    def conde( ...goals ) {
        goals = goals as List
        if( goals.size() == 0 ) {
            return succeed()
        }
        return { s ->
            return { -> mplusAll( goals, s ) }
        }
    }
    
    def anyo( goal ) {
        conde( goal, defer( this.&anyo, goal ) )
    }
    
    def nevero() {
        anyo( eq( true, false ) )
    }
    
    def alwayso() {
        anyo( eq( true, true ) )
    }
    
    def nullo( x ) {
        eq( x, [] )
    }
    
    def conso( a, d, p ) {
        eq( [ a, d ], p )
    }
    
    def pairo( p ) {
        fresh { a, d ->
            conso( a, d, p )
        }
    }
    
    def cdro( p, d ) {
        fresh { a ->
            conso( a, d, p )
        }
    }

    def caro( p, a ) {
        fresh { d ->
            conso( a, d, p )
        }
    }
    
    def listo( l ) {
        fresh { d ->
            conde( all( nullo( l ), succeed() ),
                   all( pairo( l ), cdro( l, d ), defer( this.&listo, d ) ) )
        }
    }

    def membero( x, l ) {
        fresh { a, d ->
            conde( all( caro( l, a ), eq( a, x ) ),
                   all( cdro( l, d ), defer( this.&membero, x, d ) ) )
        }
    }
    
    def fullAddero( b, x, y, r, c ) {
        conde( all( eq( 0, b ), eq( 0, x ), eq( 0, y ), eq( 0, r ), eq( 0, c ) ),
               all( eq( 1, b ), eq( 0, x ), eq( 0, y ), eq( 1, r ), eq( 0, c ) ),
               all( eq( 0, b ), eq( 1, x ), eq( 0, y ), eq( 1, r ), eq( 0, c ) ),
               all( eq( 1, b ), eq( 1, x ), eq( 0, y ), eq( 0, r ), eq( 1, c ) ),
               all( eq( 0, b ), eq( 0, x ), eq( 1, y ), eq( 1, r ), eq( 0, c ) ),
               all( eq( 1, b ), eq( 0, x ), eq( 1, y ), eq( 0, r ), eq( 1, c ) ),
               all( eq( 0, b ), eq( 1, x ), eq( 1, y ), eq( 0, r ), eq( 1, c ) ),
               all( eq( 1, b ), eq( 1, x ), eq( 1, y ), eq( 1, r ), eq( 1, c ) ) )
    }
    
    def buildNumber( int n ) {
        if( n == 0 ) {
            return []
        }
        if( n % 2 == 0 ) {
            return [ 0, buildNumber( n >> 1 ) ]
        }
        return [ 1, buildNumber( n >> 1 ) ]
    }

    Integer unpackNumber( List l ) {
       int ret = 0
       int idx = 0
       while( l ) {
           ret = ret | (int)( l[ 0 ] ) << idx++
           l = (List)l[ 1 ]
       }
       return ret 
    }
    
    List buildList( List l ) {
        if( !l ) {
            return []
        }
        return [ l.head(), buildList( l.tail() ) ]
    }
    
    List unpackList( List l ) {
        def ret = []
        while( l ) {
            ret << l[ 0 ]
            def n = l[ 1 ]
            if( n instanceof List ) {
                l = (List)n
            }
            else {
                ret << n
                l = []
            }
        }
        return ret
    }
    
    def poso( n ) {
        fresh { a, d ->
            conso( a, d, n )
        }
    }
    
    def gt1o( n ) {
        fresh { a, ad, dd ->
            eq( [ a, [ ad, dd ] ], n )
        }
    }
    
    def addero( d, n, m, r ) {
        fresh { a, c ->
            conde( all( eq( 0, d ), eq( [], m ), eq( n, r ) ),
                   all( eq( 0, d ), eq( [], n ), eq( m, r ), poso( m ) ),
                   all( eq( 1, d ), eq( [], m ), defer( this.&addero, 0, n, [ 1, [] ], r ) ),
                   all( eq( 1, d ), eq( [], n ), poso( m ), defer( this.&addero, 0, [ 1, [] ], m, r ) ),
                   all( eq( [ 1, [] ], n ), eq( [ 1, [] ], m ), eq( [ a, [ c, [] ] ], r ), fullAddero( d, 1, 1, a, c ) ),
                   all( eq( [ 1, [] ], n ), genAddero( d, n, m, r ) ),
                   all( eq( [ 1, [] ], m ), gt1o( n ), gt1o( r ), defer( this.&addero, d, [ 1, [] ], n, r ) ),
                   all( gt1o( n ), genAddero( d, n, m, r ) ) )
        }
    }
    
    def genAddero( d, n, m, r ) {
        fresh { a, b, c, e, x, y, z ->
            all( eq( [ a, x ], n ),
                 eq( [ b, y ], m ),
                 poso(y),
                 eq( [ c, z ], r ),
                 poso( z ),
                 fullAddero( d, a, b, c, e ),
                 defer( this.&addero, e, x, y, z ) )
        }
    }
    
    def pluso( n, m, k ) {
        addero( 0, n, m, k )
    }
    
    def minuso( n, m, k ) {
        pluso( m, k, n )
    }
    
    def multo( n, m, p ) {
        fresh { x, y, z ->
            conde( all( eq( [], n ), eq( [], p ) ),
                   all( poso( n ), eq( [], m ), eq( [], p ) ),
                   all( eq( [ 1, [] ], n ), poso( m ), eq( m, p ) ),
                   all( gt1o( n ), eq( [ 1, [] ], m ), eq( n, p ) ),
                   all( eq( [ 0, x ], n ),
                        poso( x ),
                        eq( [ 0, z ], p ),
                        poso( z ),
                        gt1o( m ),
                        defer( this.&multo, x, m, z ) ),
                   all( eq( [ 1, x ], n ),
                        poso( x ),
                        eq( [ 0, y ], m ),
                        poso( y ),
                        defer( this.&multo, m, n, p ) ),
                   all( eq( [ 1, x ], n ),
                        poso( x ),
                        eq( [ 1, y ], m ),
                        poso( y ),
                        defer( this.&oddMulto, x, n, m, p ) ) )
        }
    }
    
    def oddMulto( x, n, m, p ) {
        fresh { q ->
            all( boundMulto( q, p, n, m ),
                 multo( x, m, q ),
                 pluso( [ 0, q ], m, p ) )
        }
    }
    
    def boundMulto( q, p, n, m ) {
      conde( all( nullo( q ), pairo( p ) ),
             fresh { x, y, z ->
                 all( cdro(q, x),
                      cdro(p, y),
                      conde( all( nullo( n ),
                                  cdro( m, z ),
                                  defer( this.&boundMulto, x, y, z, [] ) ),
                             all( cdro( n, z ),
                             defer( this.&boundMulto, x, y, z, m ) ) ) )
             } )
    }
    
    def eqlo( n, m ) {
        fresh { a, x, b, y ->
            conde( all( eq( [], n ), eq( [], m ) ),
                   all( eq( [ 1, [] ], n ), eq( [ 1, [] ], m ) ),
                   all( eq( [ a, x ], n ), poso( x ), eq( [ b, y ], m ), poso( y ), defer( this.&eqlo, x, y ) ) )
        }
    }
    
    def ltlo( n, m ) {
        fresh { a, x, b, y ->
            conde( all( eq( [], n ), poso( m ) ),
                   all( eq( [ 1, [] ], n ), gt1o( m ) ),
                   all( eq( [ a, x ], n ), poso( x ), eq( [ b, y ], m ), poso( y ), defer( this.&ltlo, x, y ) ) )
        }
    }
    
    def lto( n, m ) {
        fresh { x ->
            conde( ltlo( n, m ),
                   all( eqlo( n, m ), poso(x), pluso( n, x, m ) ) )
        }
    }
    
    def divo( n, m, q, r ) {
        fresh { nh, nl, qh, ql, qlm, qlmr, rr, rh ->
            conde( all( eq( r, n ), eq( [], q ), ltlo( n, m ) ),
                   all( eq( [ 1, [] ], q ), eqlo( n, m ), pluso( r, m, n ), lto( r, m ) ),
                   all( ltlo( m, n ),
                        lto( r, m ),
                        poso( q ),
                        splito( n, r, nl, nh ),
                        splito( q, r, ql, qh ),
                        conde( all( eq( [], nh ), eq( [], qh ), minuso( nl, r, qlm ), multo( ql, m, qlm ) ),
                   all( poso( nh ),
                        multo( ql, m, qlm ),
                        pluso( qlm, r, qlmr ),
                        minuso( qlmr, nl, rr ),
                        splito( rr, r, [], rh ),
                        defer( this.&divo, nh, m, qh, rh ) ) ) ) )
        }
    }
    
    def splito( n, r, l, h ) {
        fresh { b, n_, a, r_, l_ ->
            conde( all( eq( [], n ),
                        eq( [], h ),
                        eq( [], l ) ),
                   all( eq( [ 0, [ b, n_ ] ], n ),
                        eq( [], r ),
                        eq( [b, n_], h ),
                        eq( [], l ) ),
                   all( eq( [1, n_], n ),
                        eq( [], r ),
                        eq( n_, h ),
                        eq( [ 1, [] ], l ) ),
                   all( eq( [ 0, [ b, n_ ] ], n ),
                        eq( [ a, r_ ], r ),
                        eq( [], l ),
                        defer( this.&splito, [ b, n_ ], r_, [], h ) ),
                   all( eq( [ 1, n_ ], n ),
                        eq( [ a, r_ ], r ),
                        eq( [ 1, [] ], l ),
                        defer( this.&splito, n_, r_, [], h ) ),
                   all( eq( [ b, n_ ], n ),
                        eq( [ a, r_ ], r ),
                        eq( [ b, l_ ], l ),
                        poso( l_ ),
                        defer( this.&splito, n_, r_, l_, h ) ) )
        }
    }
    
    @CompileDynamic
    def defer( Closure func, ...args ) {
        args = args as List
        return { s ->
            return func( *args )( s )
        }
    }
    
    Closure succeed() {
        return { s -> s }
    }
    
    Closure fail() {
        return { s -> null }
    }
    
    def fresh() { fresh( 1, null ) }
    def fresh( int n ) { return fresh( n, null ) }
    def fresh( Closure c ) { return fresh( 1, c ) }
    @CompileDynamic
    def fresh( int n, Closure c ) {
        if( c == null ) {
            if( n == 1 ) {
                return new Var()
            }
            return (1..n).collect { new Var() }
        }
        else {
            return c( *(1..c.maximumNumberOfParameters).collect { new Var() } )
        }
    }
    
    @CompileDynamic
    def run( ...args ) {
        args = args as List
        def n, v, goals, goal
        if( args[ 1 ] instanceof Closure ) {
            n = null
            v = args[ 0 ]
            goals = args.drop( 1 )
        }
        else {
            n = args[ 0 ]
            v = args[ 1 ]
            goals = args.drop( 2 )
        }

        if( goals.size() == 1 ) {
            goal = goals[ 0 ]
        }
        else {
            goal = all( *goals )
        }
        
        def ss = take( n, { -> goal( [:] ) } )
        return ss.collect { s -> reify( v, s ) }
    }
}