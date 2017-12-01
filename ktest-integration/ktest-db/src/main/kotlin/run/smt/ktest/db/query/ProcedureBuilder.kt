package run.smt.ktest.db.query

interface ProcedureBuilder<T : Any> : PlainProcedureBuilder, RespondingQuery<T>
