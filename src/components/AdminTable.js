export default function AdminTable({
  title,
  type,
  data,
  headers,
  rowRenderer,
}) {
  return (
    <section className="mb-8">
      <h2 className="text-xl font-bold text-gray-800 mb-3">{title}</h2>

      {data.length === 0 ? (
        <p className="text-gray-500 text-sm italic">No {type}s found.</p>
      ) : (
        <div className="overflow-x-auto shadow border border-gray-200 rounded-lg">
          <table className="min-w-full text-sm text-left text-gray-700">
            <thead className="bg-gray-100 text-xs uppercase text-gray-600 sticky top-0 z-10">
              <tr>
                {headers.map((h, idx) => (
                  <th key={idx} className="px-5 py-3 border-b font-bold">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>

            <tbody>
              {data.map((item, i) => (
                <tr
                  key={i}
                  className={`border-b ${
                    i % 2 === 0 ? "bg-white" : "bg-gray-50"
                  } hover:bg-blue-50 transition duration-150`}
                >
                  {rowRenderer(item).map((cell, j) => (
                    <td key={j} className="px-5 py-3 whitespace-nowrap">
                      {cell}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
