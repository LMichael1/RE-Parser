package com.lmichael.dfa

import com.beust.klaxon.Json

class State(val ID: Int, val isFinal: Boolean, val isError: Boolean, val Paths: Map<String, Int>){
}

class DFA(val initialStateID: Int = 0, val alphabet: List<String> = listOf(), val states: List<State> = listOf()){
    fun checkDFA(): Boolean{
        var flag = true
        if (states.count()==0) flag=false
        if (states.filter { it.ID==initialStateID }.count()==0) flag = false
        if (states.filter { it.isFinal==true }.count()==0) flag = false
        for (s in states){
            if (states.filter { s.ID==it.ID }.count()!=1) flag = false
            if (s.Paths.count()!=alphabet.count()) flag=false
            for (p in s.Paths){
                if(!alphabet.contains(p.key)) flag = false
            }
        }
        return flag
    }
    @Json(ignored = true)
    var history: ArrayList<String> = ArrayList()
    fun processString(str: String): Boolean{
        history.clear()
        var current: Int? = initialStateID
        history.add("Состояние: " + current.toString())
        if(!str.isEmpty()) {
            for (c in str) {
                if (!alphabet.contains(c.toString())) return false
            }

            for (c in str) {
                history.add("Путь: " + c.toString())
                if (states.filter { it.ID == current }[0].isError) {
                    history.add("Состояние: Ошибка")
                    return false
                }
                current = states.filter { it.ID == current }[0].Paths[c.toString()]
                history.add("Состояние: " + current.toString())
            }
        }
        if (states.filter { it.ID==current }[0].isFinal)return true
        return false
    }

    fun minimize(): DFA? {
        val table: Array<Array<Char>> = Array(states.count(), { Array(states.count(), {' '}) })

        for (i in 0 until states.count()){
            table[i][i]='v'
        }

        for (i in 1 until states.count()){
            for (j in 0 until i){
                table[i][j]='*'
            }
        }

        val finalStates: List<State> = states.filter { it.isFinal }
        val nonfinalStates: List<State> = states.filter { !it.isFinal }
        for (i in 0 until states.count()-1){
            for (j in i+1 until states.count()){
                if ((nonfinalStates.contains(states[i]) && finalStates.contains(states[j])) || (finalStates.contains(states[i]) && nonfinalStates.contains(states[j]))){
                    table[i][j]='x'
                }
            }
        }

        val t2: MutableList<MutableList<Int>> = mutableListOf()

        for (i in 0 until states.count()){
            for (j in 0 until states.count()){
                if (table[i][j]==' ') t2.add(mutableListOf(i, j))
            }
        }

        val marksList: MutableList<Char> = mutableListOf()

        val table2: Array<Array<MutableList<Int>>> = Array(t2.count(), { Array(alphabet.count()+1, { mutableListOf<Int>(-1, -1) } ) } )

        for(i in 0 until t2.count()){
            marksList.add(' ')
            table2[i][0]=t2[i]
        }
        var flag1: Boolean = true
        var tmp: Int = marksList.filter { it==' ' }.count()
        while (flag1) {
            for (i in 0 until t2.count()) {
                var flag: Boolean = true
                for (j in 1 until alphabet.count() + 1) {
                    table2[i][j][0]=states.indexOf(states.filter { it.ID == states[table2[i][0][0]].Paths[alphabet[j - 1]] }[0])
                    table2[i][j][1]=states.indexOf(states.filter { it.ID == states[table2[i][0][1]].Paths[alphabet[j - 1]] }[0])
                    table2[i][j].sort()
                    if (table[table2[i][j][0]][table2[i][j][1]] == 'x') {
                        table[table2[i][0][0]][table2[i][0][1]]='x'
                        marksList[i] = 'x'
                    }
                    if (table[table2[i][j][0]][table2[i][j][1]] != 'v') flag = false
                }
                if (flag) {
                    table[table2[i][0][0]][table2[i][0][1]]='v'
                    marksList[i] = 'v'
                }
            }
            if (marksList.filter { it==' ' }.count()==0 || marksList.filter { it==' ' }.count()==tmp){
                flag1=false
            }
            tmp = marksList.filter { it==' ' }.count()
        }

        if(marksList.filter { it==' ' }.count()>0){
            for (i in marksList.filter { it==' ' }){
                table[table2[marksList.indexOf(i)][0][0]][table2[marksList.indexOf(i)][0][1]]='v'
            }
        }
        //ПОКА ВСЁ ХОРОШО
        val newStates: MutableList<MutableList<Int>> = mutableListOf()
        val newStatesFinal: MutableList<Boolean> = mutableListOf()
        val newStatesError: MutableList<Boolean> = mutableListOf()

        for (i in 0 until states.count()){
            val tmpList: MutableList<MutableList<Int>> = mutableListOf()
            for (j in i+1 until states.count()){
                if (table[i][j]=='v'){
                    tmpList.add(mutableListOf(i, j))
                }
            }
            if (tmpList.count()==1 && newStates.filter { it.contains(tmpList[0][0]) && it.contains(tmpList[0][1]) }.count()==0){
                newStates.add(tmpList[0])
                if (tmpList[0].filter { states[it].isFinal }.count()==tmpList[0].count()){
                    newStatesFinal.add(true)
                }
                else{
                    newStatesFinal.add(false)
                }
                if (tmpList[0].filter { states[it].isError }.count()==tmpList[0].count()){
                    newStatesError.add(true)
                }
                else{
                    newStatesError.add(false)
                }
            }
            else if(tmpList.count()>1){
                val temp: MutableList<Int> = mutableListOf()
                temp.add(tmpList[0][0])
                temp.add(tmpList[0][1])
                for (k in 1 until tmpList.count()){
                    temp.add(tmpList[k][1])
                }
                newStates.add(temp)
                if (temp.filter { states[it].isFinal }.count()==temp.count()){
                    newStatesFinal.add(true)
                }
                else{
                    newStatesFinal.add(false)
                }
                if (temp.filter { states[it].isError }.count()==temp.count()){
                    newStatesError.add(true)
                }
                else{
                    newStatesError.add(false)
                }
            }
        }
        for (i in 0 until states.count()) {
            if(table[i].filter { it=='v' }.count()==1  && newStates.filter { it.contains(i) }.count()==0){
                newStates.add(mutableListOf(i))
                if (states[i].isFinal){
                    newStatesFinal.add(true)
                }
                else{
                    newStatesFinal.add(false)
                }
                if (states[i].isError){
                    newStatesError.add(true)
                }
                else{
                    newStatesError.add(false)
                }
            }

        }

        val table3: Array<Array<MutableList<Int>>> = Array(newStates.count(), { Array(alphabet.count(), { mutableListOf<Int>() } ) } )
        for (i in 0 until newStates.count()){
            for (j in 0 until alphabet.count()){
                for (k in newStates[i]){
                    table3[i][j].add(states.indexOf(states.filter { it.ID == states[k].Paths[alphabet[j]] }[0]))
                }
            }
        }

        for (i in 0 until newStates.count()){
            for (j in 0 until alphabet.count()){
                table3[i][j]=newStates.filter { it.contains(table3[i][j][0]) }[0]
            }
        }

        val minimizedStates: MutableList<State> = mutableListOf()

        for (i in 0 until newStates.count()){
            val tmpMap: MutableMap<String, Int> = mutableMapOf()
            for (j in 0 until alphabet.count()){
                tmpMap[alphabet[j]] = newStates.indexOf(table3[i][j])
            }
            val newState = State(i,newStatesFinal[i],newStatesError[i], tmpMap)
            minimizedStates.add(newState)
        }

        val init = newStates.indexOf(newStates.filter { it.contains(initialStateID) }[0])

        return DFA(init, alphabet, minimizedStates);
    }
}