DROP TABLE IF EXISTS public.user_stats;

CREATE TABLE public.user_stats (
    id integer NOT NULL,
    user_id integer,
    approved_questions integer,
    submitted_questions integer,
    public_requests integer,
    requests_submitted integer,
    tournaments_participated integer,
    tournaments_score integer
);

DROP SEQUENCE IF EXISTS public.user_stats_id_seq;

CREATE SEQUENCE public.user_stats_id_seq 
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.user_stats_id_seq OWNED BY public.user_stats.id;

COPY public.user_stats (id, user_id, approved_questions, submitted_questions, public_requests, requests_submitted, tournaments_participated, tournaments_score) FROM stdin WITH(FORMAT CSV);
1,651,1,1,1,1,1,1
2,646,1,1,1,1,1,1
3,647,1,1,1,1,1,1
4,655,1,1,1,1,1,1
5,627,1,1,1,1,1,1
6,673,1,1,1,1,1,1
7,653,1,1,1,1,1,1
8,635,1,1,1,1,1,1
9,633,1,1,1,1,1,1
10,669,1,1,1,1,1,1
11,648,1,1,1,1,1,1
12,650,1,1,1,1,1,1
13,643,1,1,1,1,1,1
14,674,1,1,1,1,1,1
15,661,1,1,1,1,1,1
16,660,1,1,1,1,1,1
17,662,1,1,1,1,1,1
18,665,1,1,1,1,1,1
19,638,1,1,1,1,1,1
20,677,1,1,1,1,1,1
21,670,1,1,1,1,1,1
22,639,1,1,1,1,1,1
23,654,1,1,1,1,1,1
24,645,1,1,1,1,1,1
25,641,1,1,1,1,1,1
26,630,1,1,1,1,1,1
27,631,1,1,1,1,1,1
28,640,1,1,1,1,1,1
29,649,1,1,1,1,1,1
30,617,1,1,1,1,1,1
31,632,1,1,1,1,1,1
32,671,1,1,1,1,1,1
33,621,1,1,1,1,1,1
34,668,1,1,1,1,1,1
35,636,1,1,1,1,1,1
36,623,1,1,1,1,1,1
37,675,1,1,1,1,1,1
38,672,1,1,1,1,1,1
39,624,1,1,1,1,1,1
40,625,1,1,1,1,1,1
41,664,1,1,1,1,1,1
42,637,1,1,1,1,1,1
43,628,1,1,1,1,1,1
44,629,1,1,1,1,1,1
45,658,1,1,1,1,1,1
46,656,1,1,1,1,1,1
47,616,1,1,1,1,1,1
48,619,1,1,1,1,1,1
49,620,1,1,1,1,1,1
50,626,1,1,1,1,1,1
51,659,1,1,1,1,1,1
52,657,1,1,1,1,1,1
53,618,1,1,1,1,1,1
54,634,1,1,1,1,1,1
55,667,1,1,1,1,1,1
56,642,1,1,1,1,1,1
57,663,1,1,1,1,1,1
58,644,1,1,1,1,1,1
59,666,1,1,1,1,1,1
60,676,1,1,1,1,1,1
61,652,1,1,1,1,1,1
62,622,1,1,1,1,1,1
63,737,1,1,1,1,1,1
64,692,1,1,1,1,1,1
65,678,1,1,1,1,1,1
66,679,1,1,1,1,1,1
67,680,1,1,1,1,1,1
68,681,1,1,1,1,1,1
69,682,1,1,1,1,1,1
70,683,1,1,1,1,1,1
71,684,1,1,1,1,1,1
72,685,1,1,1,1,1,1
73,686,1,1,1,1,1,1
74,687,1,1,1,1,1,1
75,688,1,1,1,1,1,1
76,689,1,1,1,1,1,1
77,690,1,1,1,1,1,1
78,691,1,1,1,1,1,1
79,693,1,1,1,1,1,1
80,694,1,1,1,1,1,1
81,695,1,1,1,1,1,1
82,696,1,1,1,1,1,1
83,697,1,1,1,1,1,1
84,698,1,1,1,1,1,1
85,699,1,1,1,1,1,1
86,700,1,1,1,1,1,1
87,701,1,1,1,1,1,1
88,702,1,1,1,1,1,1
89,703,1,1,1,1,1,1
90,704,1,1,1,1,1,1
91,705,1,1,1,1,1,1
92,706,1,1,1,1,1,1
93,707,1,1,1,1,1,1
94,708,1,1,1,1,1,1
95,709,1,1,1,1,1,1
96,710,1,1,1,1,1,1
97,711,1,1,1,1,1,1
98,712,1,1,1,1,1,1
99,713,1,1,1,1,1,1
100,714,1,1,1,1,1,1
101,715,1,1,1,1,1,1
102,716,1,1,1,1,1,1
103,717,1,1,1,1,1,1
104,718,1,1,1,1,1,1
105,719,1,1,1,1,1,1
106,720,1,1,1,1,1,1
107,721,1,1,1,1,1,1
108,722,1,1,1,1,1,1
109,723,1,1,1,1,1,1
110,724,1,1,1,1,1,1
111,725,1,1,1,1,1,1
112,726,1,1,1,1,1,1
113,727,1,1,1,1,1,1
114,728,1,1,1,1,1,1
115,729,1,1,1,1,1,1
116,730,1,1,1,1,1,1
117,731,1,1,1,1,1,1
118,732,1,1,1,1,1,1
119,733,1,1,1,1,1,1
120,734,1,1,1,1,1,1
121,735,1,1,1,1,1,1
122,736,1,1,1,1,1,1
\.
