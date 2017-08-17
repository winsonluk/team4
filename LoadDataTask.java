/**
 * USAGE:
 * 
 * new LoadDataTask("http://your-url.com");
 **/

private class LoadDataTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        try {
            // Contact server for search results
            URL url = new URL(params[0]);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null)
                builder.append(line);

            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String results) {
        // Do something here
    }
}
