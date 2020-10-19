import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.OutParameters;

import java.sql.Types;

public class StoredProcedureCalls {

    public void callProcedure()
    {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test_callProcedure");
        jdbi.useHandle(handle -> {
            //Create a call procedure in H2
            handle.execute("CREATE ALIAS NEXT_PRIME AS $$\n" +
                    "String nextPrime(String value) {\n" +
                    "    return new BigInteger(value).nextProbablePrime().toString();\n" +
                    "}\n" +
                    "$$;");

            //Call Handle.createCall() with the SQL statement.
            OutParameters result = handle
                    .createCall("{:prime = call NEXT_PRIME(:a)}")
                    .bind("a", 13)
                    //Register out parameters, the values that will be returned from the stored procedure call.
                    // This tells JDBC what data type to expect for each out parameter.
                    .registerOutParameter("prime", Types.INTEGER)
                    .invoke();

            //Now we can extract the result(s) from OutParameters:
            int prime = result.getInt("prime");
            System.out.println(prime);
        });
    }

    public static void main(String[] args) {
        StoredProcedureCalls calls = new StoredProcedureCalls();
        calls.callProcedure();
    }
}
