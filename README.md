A miniKanren in Groovy.

[![Build Status](https://travis-ci.org/timyates/groovy-logic.png)](https://travis-ci.org/timyates/groovy-logic)

Basically a port of https://github.com/spariev/mini_kanren to Groovy.

Currently experimental, and could do with some help to make it nicer/faster/more useful.

For a simple example, say you have 2 lists, and you need to find the common members, you could do:

```groovy
import com.bloidonia.logic.Logic

new Logic().with {
    
    // Create a fresh logic variable `q`
    fresh { q ->
    
        // Find all values for q which are in [ 1,2,3 ] and [ 2,3,4 ]
        assert run( q, membero( q, buildList( [ 1, 2, 3 ] ) ),
                       membero( q, buildList( [ 2, 3, 4 ] ) ) ) == [ 2, 3 ]
    }
}
```