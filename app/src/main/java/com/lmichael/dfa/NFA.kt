package com.lmichael.dfa

import java.util.*
import kotlin.collections.ArrayList


class NFAState(val ID: Int, var isFinal: Boolean, var isError: Boolean, val Paths: MutableMap<String, MutableList<Int>>, val LambdaPaths: ArrayList<Int>){
}

class NFA (var initialStateID: Int = 0, var alphabet: MutableList<String> = mutableListOf(), var states: MutableList<NFAState> = mutableListOf()) {
    fun checkNFA(): Boolean{
        var flag = true
        if (states.count()==0) flag=false
        if (states.filter { it.ID==initialStateID }.count()==0) flag = false
        if (states.filter { it.isFinal==true }.count()==0) flag = false
        for (s in states){
            if (states.filter { s.ID==it.ID }.count()!=1) flag = false
            for (p in s.Paths){
                if(!alphabet.contains(p.key)) flag = false
            }
        }
        return flag
    }

    fun regexToNFA(re: String){
        states.clear()
        alphabet.clear()
        nfa_size=0
        tmpstates.clear()
        stateStack.clear()
        rebuiltToNFA(rebuildRegex(re))
        val final = stateStack.pop()
        val start = stateStack.pop()
        tmpstates[final].isFinal=true
        initialStateID=start
        states=tmpstates
    }

    private fun rebuildRegex (re: String): String{
        var result = ""
        for (i in 0 until re.length){
            val s1 = re[i]
            if (i<re.length-1){
                val s2 = re[i+1]
                result+=s1
                if (s1 != '(' && s2 != ')' && s1 != '+' && s2!='+' && s2 != '*'){
                    result+='.'
                }
            }
        }

        result += re[re.length-1]
        var postfix = ""
        val op: Stack<Char> = Stack()

        for (i in 0 until result.length){
            if (result[i].isLetterOrDigit()){
                postfix+=result[i]
                continue
            }
            when (result[i]){
                '(' -> op.push(result[i])
                ')' -> {
                    while (op.peek()!= '('){
                        postfix+=op.pop()
                    }
                    op.pop()
                }
                else -> {
                    while (op.count()!=0){
                        val c=op.peek()
                        if (priority(c) >= priority(result[i])){
                            postfix+=op.pop()
                        }
                        else break
                    }
                    op.push(result[i])
                }
            }
        }
        while (op.count()!=0){
            postfix+=op.pop()
        }
        return postfix
    }

    private fun priority(c: Char): Int{
        when (c){
            '*' -> return 3
            '.' -> return 2
            '+' -> return 1
            else -> return 0
        }
    }

    val stateStack: Stack<Int> = Stack()
    var nfa_size = 0
    val tmpstates: MutableList<NFAState> = mutableListOf()

    private fun rebuiltToNFA(postfix: String){
        for (i in 0 until postfix.length){
            if (postfix[i].isLetterOrDigit()){
                symbol(postfix[i].toString())
                continue
            }
            when (postfix[i]){
                '*' -> star()
                '.' -> dot()
                '+' -> plus()
            }
        }
    }

    private fun symbol(s: String){
        if (!alphabet.contains(s)) alphabet.add(s)
        tmpstates.add(NFAState(tmpstates.count(), false, false, mutableMapOf(), arrayListOf()))
        tmpstates.add(NFAState(tmpstates.count(), false, false, mutableMapOf(), arrayListOf()))
        if (tmpstates[nfa_size].Paths[s]==null) tmpstates[nfa_size].Paths[s]= mutableListOf()
        tmpstates[nfa_size].Paths[s]?.add(nfa_size+1)
        stateStack.push(nfa_size)
        nfa_size+=1
        stateStack.push(nfa_size)
        nfa_size+=1
    }

    private fun plus(){
        tmpstates.add(NFAState(tmpstates.count(), false, false, mutableMapOf(), arrayListOf()))
        tmpstates.add(NFAState(tmpstates.count(), false, false, mutableMapOf(), arrayListOf()))
        val d = stateStack.pop()
        val c = stateStack.pop()
        val b = stateStack.pop()
        val a = stateStack.pop()

        tmpstates[nfa_size].LambdaPaths.add(a)
        tmpstates[nfa_size].LambdaPaths.add(c)
        tmpstates[b].LambdaPaths.add(nfa_size+1)
        tmpstates[d].LambdaPaths.add(nfa_size+1)
        stateStack.push(nfa_size)
        nfa_size+=1
        stateStack.push(nfa_size)
        nfa_size+=1
    }

    private fun dot(){
        val d = stateStack.pop()
        val c = stateStack.pop()
        val b = stateStack.pop()
        val a = stateStack.pop()
        tmpstates[b].LambdaPaths.add(c)
        stateStack.push(a)
        stateStack.push(d)
    }

    private fun star(){
        tmpstates.add(NFAState(tmpstates.count(), false, false, mutableMapOf(), arrayListOf()))
        tmpstates.add(NFAState(tmpstates.count(), false, false, mutableMapOf(), arrayListOf()))
        val b = stateStack.pop()
        val a = stateStack.pop()
        tmpstates[nfa_size].LambdaPaths.add(a)
        tmpstates[nfa_size].LambdaPaths.add(nfa_size+1)
        tmpstates[b].LambdaPaths.add(a)
        tmpstates[b].LambdaPaths.add(nfa_size+1)
        stateStack.push(nfa_size)
        nfa_size+=1
        stateStack.push(nfa_size)
        nfa_size+=1
    }

    fun Determine(): DFA?{
        if (!checkNFA()) return null
        class TableRow(var st: MutableList<Int>, var alph: Array<MutableList<Int>>)

        val table: ArrayList<TableRow> = ArrayList()

        table.add(TableRow(mutableListOf(), Array(alphabet.count()) { mutableListOf(-1) }))
        table[0].st.add(initialStateID)
        table[0].st=getLambda(table[0].st)

        var flag = true
        var j = 0
        while (flag) {
            val size = table.count()
            for (i in 0 until alphabet.count()) {
                if (table[j].st[0]==-1) continue
                table[j].alph[i].clear()
                for (s in table[j].st) {
                    val tmp: NFAState = states.filter { it.ID == s }[0]
                    if (tmp.Paths[alphabet[i]] != null) {
                        for (p in tmp.Paths[alphabet[i]]!!) {
                            if (!table[j].alph[i].contains(p)) {
                                table[j].alph[i].add(p)
                            }
                        }
                    }
                }
                table[j].alph[i] = getLambda(table[j].alph[i])
                if (table[j].alph[i].count()==0) table[j].alph[i].add(-1)
                if (table.filter { it.st == table[j].alph[i] }.count() == 0) {
                    table.add(TableRow(table[j].alph[i], Array(alphabet.count()) { mutableListOf(-1) }))
                }
            }
            if (table.count()==size && j==size-1){
                flag=false
            }
            else{
                j++
            }
        }

        val dfastates = mutableListOf<State>()
        for (i in 0 until table.count()){
            var final = false
            for (s in states.filter { it.isFinal }) {
                if (table[i].st.contains(s.ID)) final=true
            }
            var error = false
            for (s in states.filter { it.isError }) {
                if (table[i].st.contains(s.ID)) error=true
            }
            val paths = mutableMapOf<String, Int>()
            for (k in 0 until alphabet.count()){
                val row = table.filter { it.st==table[i].alph[k] }[0]
                paths[alphabet[k]]=table.indexOf(row)
            }
            dfastates.add(State(i, final, error, paths))
        }
        val dfa = DFA(0,alphabet,dfastates)
        if (dfa.checkDFA()) return dfa
        return null
    }

    private fun getLambda(list: MutableList<Int>): MutableList<Int>{
        var fl = true
        while (fl) {
            val size = list.count()
            var j = 0
            var cnt = 0
            for (s in j until size) {
                val tmp: NFAState = states.filter { it.ID == list[s] }[0]
                if (tmp.LambdaPaths.count() > 0) {
                    for (p in tmp.LambdaPaths) {
                        if (!list.contains(p)) {
                            list.add(p)
                            cnt++
                        }
                    }
                }
            }
            if (cnt == 0) {
                fl = false
            }
            else{
                j = size
            }
        }
        list.sort()
        return list
    }
}